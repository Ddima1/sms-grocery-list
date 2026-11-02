# sms-grocery-list

# PROBLEM
When I run out of something, I forget to write it down to buy. For instance, if I run out of Orange Juice, I often forget to buy more, and then when I go to the grocery store, I either have to spend time making a list (which I often don't do) or hope I remember.

# SOLUTION
A Java program that writes to a text file and, on a set date, texts you (via an SMS-to-email service) when you need it; you can then clear it from your device/terminal when you no longer need it. You can add or remove an item to be reminded on the same device. Texting is the primary way to communicate with the device.

Requires:
- Languages: Java, potentially Bash
- Libraries: util, Net, and lists
- External: AgentMail 
- AgentMail is a free email API. Will use SMS to email conversion.

# DEVELOPMENT PLAN
- Outputs a list with an item and quantity/status
- Can update an item status
- Can add/remove items from a list
- Can output the list at a pre-programmed time and date
- Can update the output time and date
- Can text the list to a user
- User can text the program -- SMS is a console

IMPORTANT & URGENT: Store List, Update List, Get List via phone
NOT IMPORTANT & URGENT: Lists can be searched and sorted; the whole list can be shown, including items not needed to be shown
IMPORTANT & NOT URGENT: Other users can update one greater list
NOT IMPORTANT & NOT URGENT: Access via a website, visual aids

# MVP
User can access the shopping list from their phone via SMS and update it via SMS. User can also specify a recurring time and date to display it, or request it.

<img width="567" height="352" alt="SMSShopping drawio(1)" src="https://github.com/user-attachments/assets/1114d4d3-d100-4642-8018-c113c12a0c57" />
