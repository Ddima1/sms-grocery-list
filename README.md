# sms-grocery-list

## PROBLEM
When I run out of something, I forget to write it down to buy. For instance, if I run out of Orange Juice, I often forget to buy more, and then when I go to the grocery store, I either have to spend time making a list (which I often don't do) or hope I remember.

## SOLUTION
A Java program that writes to a text file and, on a set date, texts you when you ask it; you can then clear it from your device/terminal when you no longer need it. You can add or remove an item to be reminded on the same device. Texting is the primary way to communicate with the device.

Requirements:
- Languages: Java 26.0.2.1 and Python 3.15.2 -- Most recent should work.
- External requirements: TextNow account (free), US (+1) Phone number.
- Hardware: Server or device capable of running for the desired duration of use.

## DEVELOPMENT PLAN
V1.0:
- Outputs a list with an item and quantity/inStock
- Can update an item's inStock status
- Can add/remove items from a list
- Can text the list to a user
- User can text the program -- SMS is a console
V1.1 and beyond:
- Multiple Clients can connect
- Output at a specified date and time
- Poll the thesaurus for interpret command
- Poll from the internet/shopping website for groceries.

IMPORTANT & URGENT: Store List, Update List, Get List via phone
NOT IMPORTANT & URGENT: Lists can be searched and sorted; the whole list can be shown, including items not needed to be shown
IMPORTANT & NOT URGENT: Other users can update one greater list
NOT IMPORTANT & NOT URGENT: Access via a website, visual aids

## EXPLANATION
User can access the shopping list from their phone via SMS and update it via SMS. Allows for multiple client connections per host as of V1.1.

<img width="1022" height="812" alt="FlowchartSMSGroceryList drawio" src="https://github.com/user-attachments/assets/db4f7504-575a-4b8c-aeb4-49f42551ca4e" />

Explanation of the flow chart: 
For every message sent to TextNow, it requires a SID (login ID), Username, Recipient (+1XXXXXXXXXX), and a port—this is automatically assigned and is user ID + 5000. This is specified during setup on the server end. On boot, the Python sidecar is booted up and sends a boot-up message to the user and every 5000ms polls TextNow for a message. The client only reads the most recent message sent before replying.
On polling message the sidecar sends this as a JSON to client.receiveSMS()  where it sends it as a packet with the user ID and body to the host. 
The host then interprets the message and reparses it as a packet with a CMD ID and an argument (item to add/remove). The host then runs that command on the List. It then uses the user ID specified by the user to parse a message packet through the interface to the client, where the message body is parsed as JSON and sent back to the sidecar.
The sidecar then parses the JSON for the recipient for that client, to the textNow Website, which sends a text to the user of the command.
