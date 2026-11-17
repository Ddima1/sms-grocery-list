package userconnection;

import data.Item;
import data.Packet;
import exceptions.BadPacketException;
import exceptions.SidecarException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Host implements Runnable{
    private static List<Item> groceries = new ArrayList<>();
    private final SmsResponseListener[] listeners;
    private boolean running = true;
    private volatile String userMessage;
    private int userID = -1;

    public Host(SmsResponseListener... listeners){
        this.listeners = listeners;
    }

    public List<Item> getGroceries() {
        return groceries;
    }

    public Packet parsePacket(Packet packet) {
        ArrayList<String> argsReturn = new ArrayList<>();
        switch (packet.CMD) {
            case 0: // End
                IO.println("Goodbye!");
                running = false;
                break;
            case 1: { // Get items
                List<String> needed = new ArrayList<>();
                for (Item item : groceries) {
                    if (!item.getInStock()) {
                        needed.add(item.toString());
                    }
                }

                if (needed.isEmpty()) {
                    argsReturn.add("You don't need to buy anything! Maybe you should treat yourself!");
                    // Old: SUCCESSFUL: NO ITEMS TO SHOW
                } else {
                    argsReturn.add("Reminding you to buy: " + String.join(", ", needed) + ".");
                    // Old: "Data.Item: " + item.getName()
                }
                break;
            }
            case 2: // Add item
                if (packet.args.length >= 2) {
                    String name = packet.args[0];
                    int quantity = Integer.parseInt(packet.args[1]);
                    addGroceries(new Item(name, quantity));
                    argsReturn.add("I added " + name + " to your list!");
                } else {
                    argsReturn.add("Unable to add item! Check your spelling!");
                }
                break;
            case 3: // Update item quantity
                if (packet.args.length >= 2) {
                    String name = packet.args[0];
                    int quantity = Integer.parseInt(packet.args[1]);
                    for (Item item : groceries) {
                        if (item.getName().equalsIgnoreCase(name)) {
                            item.setQuantity(quantity);
                            argsReturn.add("Set " + name +"'s quantity to "+ quantity);
                            break;
                        }
                    }
                } else {
                    argsReturn.add("Unable to update! Try adding the item to your list first!");
                }

                break;
            case 4: // Remove item
                if (packet.args.length >= 1) {
                    String name = packet.args[0];
                    groceries.removeIf(item -> item.getName().equalsIgnoreCase(name));
                    argsReturn.add("Removed " + name + " from your list!");
                } else {
                    argsReturn.add("Failed to remove item. Maybe the item is already removed.");
                }
                break;
            default:
                throw new BadPacketException("Unknown command: " + packet.CMD + " ID: " + packet.ID + " Args: " + String.join(", ", packet.args));
        }
        return new Packet(argsReturn.toArray(new String[0]));
    }
    public void addGroceries(Object grocery) {
        if(grocery instanceof Item e)
            groceries.add(e);
    }
    public boolean getStaus(){return running;}
    public void parseText(Packet input) {
        userID = Integer.parseInt(input.args[0]);
        userMessage = input.args[1];
    }

    @Override
    public void run() {
        String[] args;
        IO.println("[HOST ACTIVE]");

        while(running){
            //String input = IO.readln("\nAwaiting User Input:\n");
            String input = userMessage;
            userMessage = null;
            StringBuilder tempString = new StringBuilder();
            Packet pongPacket;
            if(input != null && !input.isEmpty()){
                args = interpretInput(input);
                int cmdArg = Integer.parseInt(args[0]);
                for(int i = 1; i < args.length; i++){
                    tempString.append(args[i]).append(",");
                }
                args = tempString.toString().split(",");
                Packet packet = new Packet(cmdArg, args);
                // add functionality to check user args against dictionary for command and args
                // for now args[0] is command args[1-...n] are args
                try{
                    pongPacket = parsePacket(packet);

                    StringBuilder argsToParse = new StringBuilder();
                    for(String arg : pongPacket.args){
                        argsToParse.append(arg);
                    }
                    if (listeners != null) {
                        listeners[userID].onDataReceived(new Packet(argsToParse.toString()));
                    }
                    //IO.println("END OF LINE");
                } catch (BadPacketException e) {
                    IO.println(e.getMessage());
                }
            }

            try {
                Thread.sleep(500); // latency to prevent new messages being prematurely written and sent
            } catch (InterruptedException _) {}
        }
    }

    private String[] interpretInput(String input){
        input = input.toUpperCase();
        String[] dictionaryEnd = {"STOP", "KILL", "END"};
        String[] dictionaryGet = {"GET", "SHOW", "SEE", "GIVE", "WHAT"};
        String[] dictionaryAdd = {"ADD", "REMIND", "BUY", "PURCHASE", "ORDER"};
        String[] dictionaryUpdate = {"UPDATE", "PURCHASED", "BOUGHT", "ORDERED", "HAVE"};
        String[] dictionaryRemove = {"REMOVE", "DELETE", "FORGET", "IGNORE", "DONT"};

        ArrayList<String> foodList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/userconnection/FoodList.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                foodList.add(line);
            }
            foodList.sort(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int command = -1;
        String item = "";

        ArrayList<String> args = new ArrayList<>(Arrays.asList(input.toUpperCase().split(" ")));

        for(String arg : args){
            if(Arrays.asList(dictionaryEnd).contains(arg)){
                command = 0;
                break;
            } else if (Arrays.asList(dictionaryGet).contains(arg)) {
                command = 1;
                break;
            } else if (Arrays.asList(dictionaryAdd).contains(arg)) {
                command = 2;
                break;
            } else if (Arrays.asList(dictionaryUpdate).contains(arg)) {
                command = 3;
                break;
            } else if (Arrays.asList(dictionaryRemove).contains(arg)) {
                command = 4;
                break;
            }
        }

        int indexItem = -1;
        if (command > 1) {
            for (int i = 0; i < args.size(); i++) {
                if (foodList.contains(args.get(i))) {
                    item = args.get(i);
                    indexItem = i;
                    break;
                }
            }
        }

        String[] argsReturn = {Integer.toString(command), item, "0"};

        if (!item.isEmpty() && command != 4) {
            for (int j : new int[]{indexItem - 1, indexItem + 1}) { // checks before and after the food buzzword
                if (j >= 0 && j < args.size()) {
                    try {
                        Integer.parseInt(args.get(j)); // way of ensuring the packet won't cause an error later
                        argsReturn[2] = args.get(j);
                        break;
                    } catch (NumberFormatException _) {}
                }
            }
        }

        return argsReturn;
    }

    public void setGroceries(List<Item> groceries) {
        Host.groceries = groceries;
    }
}
