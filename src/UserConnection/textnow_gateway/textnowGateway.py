# Note to future me and anyone who views this code:
# As of 11/10/2026 (When this code is written) this is the first time I touched python in over a year
# Please bear with me if this code is bad <3
# Also don't mind the excess comments. It is for my use to inline document, will be removed in final build.
# Referenced https://pypi.org/project/PyTextNow/ & https://github.com/joeyagreco/pythontextnow in writing

import time
import os
import traceback
from pythontextnow import Client
from pythontextnow import ConversationService
from pythontextnow.enum import MessageDirection
from pythontextnow.model.TextMessage import TextMessage
from threading import Thread
from flask import Flask, request, jsonify

app = Flask(__name__)

USERNAME = os.environ.get("TEXTNOW_USERNAME", "").strip()
CONNECT_SID = os.environ.get("TEXTNOW_SID", "").strip().replace('"', '').replace("'", "")
RECIPIENT_PHONE = os.environ.get("RECIPIENT_PHONE", "").strip() # format +1XXXXXXXXXX
PORT = int(os.environ.get("PORT", "5000"))


POLLING_ENABLED = True # Switch to true in v1.x

# Ensures python and java logs appear
def log(*args):
    print(*args, flush=True)

client_ready = False

def patch_library_message_parsing():
    try:
        from pythontextnow.model import Message
        if not isinstance(Message, type):  # got the module instead of the class, so grab the class inside it
            Message = Message.Message

        original_from_dict = Message.from_dict

        def tolerant_from_dict(message_dict, *args, **kwargs):
            if isinstance(message_dict, dict):
                filtering = message_dict.get("conversation_filtering")
                if not isinstance(filtering, dict):
                    message_dict["conversation_filtering"] = {"first_time_contact": False}
                else:
                    filtering.setdefault("first_time_contact", False)
            return original_from_dict(message_dict, *args, **kwargs)

        Message.from_dict = staticmethod(tolerant_from_dict)
        log("Applied compatibility patch for missing 'conversation_filtering'.")
    except Exception as e:
        log(f"Could not apply compatibility patch: {type(e).__name__}: {e!r}")

patch_library_message_parsing()

try:
    log(f"Attempting initialization for user: {USERNAME}...")

    if not USERNAME or not CONNECT_SID:
        raise ValueError("TEXTNOW_USERNAME and/or TEXTNOW_SID environment variable is not set")

    Client.set_client_config(username=USERNAME, sid_cookie=CONNECT_SID)
    client_ready = True

    log("Successfully authenticated with TextNow!")
except Exception as e:
    log(f"Authentication failed during handshake step: {e}")
    client_ready = False

conversations = {}

def get_conversation(phone_number):
    if phone_number not in conversations:
        conversations[phone_number] = ConversationService(conversation_phone_numbers=[phone_number])
    return conversations[phone_number]


processed_message_ids = set()
message_queue = []

@app.route('/get-next-sms', methods=['GET'])
def get_next_sms():
    if len(message_queue) > 0:
        latest_text = message_queue.pop(0)  #  shifts the queue
        return jsonify({"body": latest_text})
    return jsonify({"body": ""})

@app.route('/send', methods=['POST'])
def send_sms():
    if not client_ready: # Checks connection to TextNow host
        return jsonify({"status": "error", "message": "TextNow client not initialized"}), 500

    data = request.get_json(silent=True) or {} # Gets JSON from client.java
    recipient = data.get("to") # Gets the recipient number
    message_text = data.get("body") # Gets the body of the message

    if not recipient or not message_text: # Checks for null
        return jsonify({"status": "error", "message": "Missing 'to' or 'body'"}), 400

    try:
        get_conversation(recipient).send_message(message=message_text) # Sends the message to recipient via TextNow
        return jsonify({"status": "success", "message": "SMS dispatched successfully"})
    except Exception as e:
        error_msg = str(e)
        log(f"Error while sending SMS: {error_msg}")

        if "403" in error_msg:
            log("Detected 403 Forbidden. If this keeps happening, copy a fresh connect.sid from your browser.")
        return jsonify({
            "message": f"Could not send message. Internal script error: {error_msg}",
            "status": "error"
        }), 500


def poll_incoming_messages():
    if not client_ready or not RECIPIENT_PHONE:
        log("Polling disabled: client not initialized.")
        return

    conversation = get_conversation(RECIPIENT_PHONE)

    last_error = None # only prints new errors

    while True:
        try:
            recent_messages = next(conversation.get_messages(num_messages=5), [])
            for msg in recent_messages:
                if msg.message_direction != MessageDirection.INCOMING or msg.read or not isinstance(msg, TextMessage):
                    continue

                msg_id = getattr(msg, 'id', None) or f"{getattr(msg, 'number', '')}_{getattr(msg, 'date_time', '')}_{msg.text}"

                if msg_id not in processed_message_ids:
                    processed_message_ids.add(msg_id)

                    message_queue.append(msg.text)
                    log(f"Intercepted message held in buffer: {msg.text}")

                    conversation.mark_as_read(message=msg)
        except Exception as e:
            error_text = f"{type(e).__name__}: {e!r}"
            if error_text != last_error:
                log(f"Poll error: {error_text}")
                traceback.print_exc()
                last_error = error_text
        else:
            last_error = None  # forgets errors if successful
        time.sleep(5)

if __name__ == '__main__':
    if POLLING_ENABLED:
        Thread(target=poll_incoming_messages, daemon=True).start()

    app.run(host='127.0.0.1', port=PORT, debug=False)