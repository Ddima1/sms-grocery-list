"""
Local sidecar that sends and receives TextNow SMS messages for the sms-grocery-list application.

The TextNow account and gateway are configured by the ``TEXTNOW_USERNAME``,
``TEXTNOW_SID``, ``RECIPIENT_PHONE``, and ``PORT`` environment variables.
``PORT`` defaults to 5000 -- this is user at index 0 in context.

The server listens only on 127.0.0.1 and provides POST /send for outbound messages and GET
/get-next-sms for retrieving queued inbound messages. A background polling thread checks the
configured recipient's conversation every five seconds when polling is enabled.

Author: Vinnie Di Maio
Version: 1.1
Created: 11/10/2026

References:
    https://pypi.org/project/PyTextNow/
    https://github.com/joeyagreco/pythontextnow
"""
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

def log(*args):
    """
    Writes flushed log line to standard output.

    Values are passed directly to print so the Python sidecar's messages
    appear promptly in the Java process's inherited console output.

    Args:
        *args: Objects to print, separated by spaces and converted to strings
            using print's normal behavior.
    """
    print(*args, flush=True)

client_ready = False

def patch_library_message_parsing():
    """
    Installs a compatibility wrapper around PyTextNow's "Message.from_dict".

    The wrapper ensures each dictionary has a dictionary-valued conversation_filtering field and that this field
    contains first_time_contact (default: False). It then delegates parsing to the original method.
    """
    try:
        from pythontextnow.model import Message
        if not isinstance(Message, type):  # got the module instead of the class, so grab the class inside it
            Message = Message.Message

        original_from_dict = Message.from_dict

        def tolerant_from_dict(message_dict, *args, **kwargs):
            """
            Adds missing conversation-filtering metadata before parsing a message.


            If message_dict is a dictionary, this function modifies it in
            place. A missing or non-dictionary conversation_filtering value
            is replaced with {"first_time_contact": False}.Other input values
            are passed to the original unchanged.

            Args:
                message_dict: Message data supplied to PyTextNow's parser.
                *args: Additional positional arguments forwarded unchanged.
                **kwargs: Additional keyword arguments forwarded unchanged.

            Returns:
                The result returned by the original Message.from_dict function.

            Raises:
                Any exception raised by the original implementation.
            """
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
    """
    Gets the conversation for a phone number

    Args:
        phone_number: TextNow phone number

    Returns:
        The cached or newly created ConversationService for the recipient's phone number.

    Raises:
        Any exception raised by ConversationService while creating the
        conversation.
    """
    if phone_number not in conversations:
        conversations[phone_number] = ConversationService(conversation_phone_numbers=[phone_number])
    return conversations[phone_number]


processed_message_ids = set()
message_queue = []

@app.route('/get-next-sms', methods=['GET'])
def get_next_sms():
    """
    Dequeues one incoming SMS for the Java client in FIFO order.

    Returns:
        A JSON response with HTTP 200 and with a body string. The contains the oldest queued SMS,
        or is empty when the queue is empty.
    """
    if len(message_queue) > 0:
        latest_text = message_queue.pop(0)  #  shifts the queue
        return jsonify({"body": latest_text})
    return jsonify({"body": ""})

@app.route('/send', methods=['POST'])
def send_sms():
    """

    Validates and sends a message through TextNow.

    Request JSON:
        to: Non-empty recipient phone number string.
        body: Non-empty SMS message text string.

    Returns:
        On success, a Flask JSON response with HTTP 200 and
        "{"status": "success", "message": "SMS dispatched successfully"}".

        Missing or empty body shows a JSON error message.
    """
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
    """
    Continuously polls TextNow for unread incoming text messages.

    Each poll requests up to five recent messages by the user. Each unseen incoming message is identified by its
    message ID, or
    by a fallback from its number, timestamp, and text. Marks the text as "read".
    The loop polls every 5000ms/5 seconds. Identical poll errors are logged only once until a successful poll
    resets the error state.
    """
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