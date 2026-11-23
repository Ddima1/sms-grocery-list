# SMS Grocery List

## PROBLEM
When I run out of something, I forget to write it down to buy. For instance, if I run out of Orange Juice, I often 
forget to buy more. Then, when I go to the grocery store, I either have to spend time making a list or hope I 
remember--often I do the latter.

## SOLUTION
A program that can store and text you a grocery list. It should allow you to update, add, and remove items, as well 
as retrieve it.

Requirements:
- Languages: Java 26.0.2.1 and Python 3.15.2 -- Most recent versions should work.
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

<img width="584" height="539" alt="EisenhowerMatrixSMSGroceryList drawio" src="https://github.com/user-attachments/assets/d6830b7e-7faf-44db-8c95-a457d1dc1bb0" />


## HOW TO GET SID AND USERNAME

All of these must be done on the website. You must wait 1-7 days if you can't send messages from the website.

To find your username, go to Settings; it should be displayed there.

To find your SID in your browser, inspect element, go to "Storage," and copy the value of "connect.sid".

<img width="1917" height="997" alt="image" src="https://github.com/user-attachments/assets/89628faa-e8f5-4889-af49-03879bc99e3d" />

## EXPLANATION OF PROGRAM
Users can access the shopping list from their phone via SMS and update it via SMS. Allows for multiple client connections per host as of V1.1.

<img width="1022" height="812" alt="FlowchartSMSGroceryList drawio" src="https://github.com/user-attachments/assets/db4f7504-575a-4b8c-aeb4-49f42551ca4e" />

Explanation of the flow chart: 

For every message sent to TextNow, it requires a SID (Session ID), Username, Recipient (+1XXXXXXXXXX), and a 
port—this is 
automatically assigned and is user ID + 5000. This is specified during setup on the server end. On boot, the Python  
sidecar starts and sends a boot-up message to the user, and every 5000ms polls TextNow for a message. The client 
only reads the most recent message sent before replying.

On polling for a message, the sidecar sends this as JSON to client.receiveSMS(),  where it sends it as a packet with the 
user ID and body to the host. 

The host then interprets the message and reparses it as a packet with a CMD ID and an argument (item to add/remove). 
The host then runs that command on the List. It then uses the user ID specified by the user to parse a message 
packet  through the interface to the client, where the message body is parsed as JSON and sent back to the sidecar.
The sidecar then parses the JSON for that client's recipient and sends it to the textNow Website, which sends a text 
to the user of the command.
