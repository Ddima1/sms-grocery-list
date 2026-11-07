import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.time.ZonedDateTime;

public class Host implements Runnable{
    private List<Item> groceries = new ArrayList<Item>();
    private ZonedDateTime dateTime = ZonedDateTime.now();
    private Calendar userCalendar = Calendar.getInstance();
    private boolean useTimeFunction = true;
    private boolean running = true;

    private Thread timeThread;

    public List<Item> getGroceries() {
        return groceries;
    }

    public Packet parsePacket(Packet packet) {
        ArrayList<String> argsReturn = new ArrayList<String>();
        switch (packet.CMD) {
            case 0: // End
                running = false;
                timeThread.interrupt();
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
                    // Old: "Item: " + item.getName()
                }
                break;
            }
            case 2: // Add item
                if (packet.args.length >= 2) {
                    String name = packet.args[0];
                    int quantity = Integer.parseInt(packet.args[1]);
                    addGroceries(new Item(name, quantity));
                    argsReturn.add("SUCCESS");
                } else {
                    argsReturn.add("FAILED");
                }
                break;
            case 3: // Update item quantity
                if (packet.args.length >= 2) {
                    String name = packet.args[0];
                    int quantity = Integer.parseInt(packet.args[1]);
                    for (Item item : groceries) {
                        if (item.getName().equalsIgnoreCase(name)) {
                            item.setQuantity(quantity);
                            argsReturn.add("SUCCESS");
                            break;
                        }
                    }
                } else {
                    argsReturn.add("FAILED");
                }

                break;
            case 4: // Remove item
                if (packet.args.length >= 1) {
                    String name = packet.args[0];
                    groceries.removeIf(item -> item.getName().equalsIgnoreCase(name));
                    argsReturn.add("SUCCESS");
                } else {
                    argsReturn.add("FAILED");
                }
                break;
            case 5: // set useTimeFunction
                if (packet.args.length >= 1) {
                    useTimeFunction = Boolean.parseBoolean(packet.args[0]);
                    argsReturn.add("SUCCESS");
                } else {
                    argsReturn.add("FAILED");
                }
                break;
            case 6: // Update Date/Time
                if (packet.args.length >= 1) {
                    setDateTime(packet.args);
                    argsReturn.add("SUCCESS");
                } else {
                    argsReturn.add("FAILED");
                }
                break;
            default:
                throw new BadPacketException("Unknown command: " + packet.CMD + " ID: " + packet.ID + " Args: " + String.join(", ", packet.args));
        }
        return new Packet(argsReturn.toArray(new String[0]));
    }
    public void addGroceries(Object grocery) {
        if(grocery instanceof Item e)
            this.groceries.add(e);
    }
    public void setGroceries(List<Item> groceries) {
        this.groceries = groceries;
    }
    public void setDateTime(String[] dtgArgs) {
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        Character[] illegalChars = new Character[]{'-', '/', '\\', '.', ',', ';', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '=', '{', '}', '[', ']', '|', '<', '>', '?', '~'};

        for (int i = 0; i < dtgArgs.length; i++) {
            if(dtgArgs[i] == null) {
                i++;
            }
            for(Character c : illegalChars) {
                dtgArgs[i] = dtgArgs[i].replace(c.toString(), "");
            }

            dtgArgs[i] = dtgArgs[i].toUpperCase().trim();
        }

        try{

            hour = Integer.parseInt(dtgArgs[3]);
            minute = Integer.parseInt(dtgArgs[4]);

            for(int i = 0; i < 3; i++) {
                if(dtgArgs[i].contains("DAY")) {
                    if(dtgArgs[i].contains("MON")) {
                        day = 1;
                    } else if (dtgArgs[i].contains("TUE")) {
                        day = 2;
                    } else if (dtgArgs[i].contains("WED")) {
                        day = 3;
                    } else if (dtgArgs[i].contains("THU")) {
                        day = 4;
                    } else if (dtgArgs[i].contains("FRI")) {
                        day = 5;
                    } else if (dtgArgs[i].contains("SAT")) {
                        day = 6;
                    } else if (dtgArgs[i].contains("SUN")) {
                        day = 7;
                    } else{
                        if(dtgArgs[i].contains("JAN")) {
                            month = Calendar.JANUARY;
                        } else if(dtgArgs[i].contains("FEB")) {
                            month = Calendar.FEBRUARY;
                        } else if(dtgArgs[i].contains("MAR")) {
                            month = Calendar.MARCH;
                        } else if(dtgArgs[i].contains("APR")) {
                            month = Calendar.APRIL;
                        } else if(dtgArgs[i].contains("MAY")) {
                            month = Calendar.MAY;
                        } else if(dtgArgs[i].contains("JUN")) {
                            month = Calendar.JUNE;
                        } else if(dtgArgs[i].contains("JUL")) {
                            month = Calendar.JULY;
                        } else if(dtgArgs[i].contains("AUG")) {
                            month = Calendar.AUGUST;
                        } else if(dtgArgs[i].contains("SEP")) {
                            month = Calendar.SEPTEMBER;
                        } else if(dtgArgs[i].contains("OCT")) {
                            month = Calendar.OCTOBER;
                        } else if(dtgArgs[i].contains("NOV")) {
                            month = Calendar.NOVEMBER;
                        } else if(dtgArgs[i].contains("DEC")) {
                            month = Calendar.DECEMBER;
                        }
                    }
                }
            }
            year = Integer.parseInt(dtgArgs[0].substring(0, 4));
        } catch (NumberFormatException _) {}


        userCalendar.set(year, month, day, hour, minute);
    }

    @Override
    public void run() {
        String[] args = null;
        IO.println("SMS-GROCERY-LIST V0.3 BY VINCENT DIMAIO TEST BUILD\n");

        int parseCMD = -1;
        String parseArgs = null;

        while(running){
            String input = IO.readln("INPUT:\n");
            StringBuilder tempString = new StringBuilder();
            Packet pongPacket = null;
            if(!input.isEmpty()){
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
                    // pongPacket will be in the future received by the client
                    // exists this way for testing at the moment
                    for(String arg : pongPacket.args){
                        IO.print(arg + " | ");
                    }
                    IO.println("END OF LINE");
                } catch (BadPacketException e) {
                    IO.println(e.getMessage());
                }
            }

            if (useTimeFunction && (timeThread == null || !timeThread.isAlive())) {
                startTimeThread();
            }
        }
    }

    private void startTimeThread() {
        timeThread = new Thread(() -> {
            while (running && useTimeFunction) {
                dateTime = ZonedDateTime.now();
                ZonedDateTime target = userCalendar.toInstant().atZone(userCalendar.getTimeZone().toZoneId());
                if (target != null) {
                    parsePacket(new Packet(1, new String[]{}));
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        timeThread.start();
    }
    private String[] interpretInput(String input){
        input = input.toUpperCase();
        String[] dictionaryEnd = {"STOP", "KILL", "END"};
        String[] dictionaryGet = {"GET", "SHOW", "SEE", "GIVE"};
        String[] dictionaryAdd = {"ADD", "REMIND", "BUY", "PURCHASE", "ORDER"};
        String[] dictionaryUpdate = {"UPDATE", "PURCHASED", "BOUGHT", "ORDERED", "HAVE"};
        String[] dictionaryRemove = {"REMOVE", "DELETE", "FORGET", "IGNORE"};

        ArrayList<String> foodList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("Src/FoodList.txt"))) {
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
        int quantity = -1;

        ArrayList<String> args = new ArrayList<>(Arrays.asList(input.toUpperCase().split(" ")));
        args.sort(null);

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
        if(command > 1){
            for(String food : foodList){
                indexItem = Arrays.binarySearch(args.toArray(new String[0]), food);
                if(indexItem >= 0){
                    item = food;
                    break;
                }
            }
        }

        if(command == 4){
            args.set(indexItem-1, "0");
        }

        String[] argsReturn = new String[3];

        argsReturn[0] = Integer.toString(command);
        if(!item.isEmpty()){
            argsReturn[2] = args.get(indexItem-1);
            argsReturn[1] = args.get(indexItem);
            try{
                Integer.parseInt(argsReturn[1]); // Way to ensure that arg[1] is actually, in fact, an integer
            } catch (NumberFormatException e){
                argsReturn[2] = "0";
            }
        }

        return argsReturn;
    }
}
