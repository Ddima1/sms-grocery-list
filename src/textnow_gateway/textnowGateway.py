# Note to future me and anyone who views this code:
# As of 11/10/2026 (When this code is written) this is the first time I touched python in over a year
# Please bear with me if this code is bad <3
# Also don't mind the excess comments. It is for my use to inline document, will be removed in final build.
# Referenced https://pypi.org/project/PyTextNow/ & https://github.com/joeyagreco/pythontextnow in writing

import time
import os
from pythontextnow import Client
from pythontextnow import ConversationService
from pythontextnow.enum import MessageDirection
from pythontextnow.model import TextMessage
from threading import Thread
from flask import Flask, request, jsonify

app = Flask(__name__)

USERNAME = os.environ.get("TEXTNOW_USERNAME", "").strip()
CONNECT_SID = os.environ.get("TEXTNOW_SID", "").strip().replace('"', '').replace("'", "")
RECIPIENT_PHONE = os.environ.get("MONICA", "").strip() # format +1XXXXXXXXXX


POLLING_ENABLED = False # Switch to true in v1.x

# Ensures python and java logs appear
def log(*args):
    print(*args, flush=True)

client_ready = False

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
    JAVA_WEBHOOK_URL = "http://127.0.0.1" # localhost

    while True:
        try:
            unread_messages = client.get_unread()
            if msg_id not in processed_message_ids: # Checks if the message has already been read
                processed_message_ids.add(msg_id)
                payload = { # JSON
                    "sender": msg.number,
                    "messageBody": msg.text
                }
                try:
                    response = requests.post(JAVA_WEBHOOK_URL, json=payload, timeout=5) # timeout prevents hang errors
                    if response.status_code == 200: # HTTP 200 OK
                        msg.mark_as_read()
                except Exception as java_err:
                    print(f"Java endpoint offline. Postponing forward: {java_err}")
                    processed_message_ids.remove(msg_id)
        except Exception as e:
            pass
            time.sleep(5)

if __name__ == '__main__':
    if POLLING_ENABLED:
        Thread(target=poll_incoming_messages, daemon=True).start()

    app.run(host='127.0.0.1', port=5000, debug=False)