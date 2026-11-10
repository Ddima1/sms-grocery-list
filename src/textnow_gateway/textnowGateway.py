# Note to future me and anyone who views this code:
# As of 11/10/2026 (When this code is written) this is the first time I touched python in over a year
# Please bear with me if this code is bad <3
# Also don't mind the excess comments. It is for my use to inline document, will be removed in final build.
# Referenced https://pypi.org/project/PyTextNow/ & https://github.com/joeyagreco/pythontextnow in writing

import time
import os
from threading import Thread
from flask import Flask, request, jsonify
import pytextnow

app = Flask(__name__)

USERNAME = os.environ.get("TEXTNOW_USERNAME")
CONNECT_SID = os.environ.get("TEXTNOW_SID")

try:
    client = pytextnow.Client(USERNAME, sid=CONNECT_SID)
    # print("Successfully authenticated with TextNow!")
except Exception as e:
    # print(f"Authentication failed: {e}")
    client = None

processedMessageIDs = set()

@app.route('/send', methods=['POST'])
def send_sms():
    if not client: # Checks connection to TextNow host
        return jsonify({"status": "error", "message": "TextNow client not initialized"}), 500

    data = request.json # Gets JSON from client.java
    recipient = data.get("to") # Gets the recipient number
    message_text = data.get("body") # Gets the body of the message

    if not recipient or not message_text: # Checks for null
        return jsonify({"status": "error", "message": "Missing 'to' or 'body'"}), 400

    try:
        client.send_sms(recipient, message_text) # Parses the message to TextMe then sends it to recipient
        return jsonify({"status": "success", "message": "SMS dispatched successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500



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
    Thread(target=poll_incoming_messages, daemon=True).start()
    app.run(host='127.0.0.1', port=5000)