import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.time.ZonedDateTime;

public class Host implements Runnable{
    private List<Item> groceries = new ArrayList<Item>();

    public List<Item> getGroceries() {
        return groceries;
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
        IO.println("SMS-GROCERY-LIST V0.3 BY VINNIE DI MAIO TEST BUILD\n");

        int parseCMD = -1;
        String parseArgs = null;
        while(useTimeFunction){
            dateTime = ZonedDateTime.now();

            if(dateTime.equals(userCalendar)) {
                parsePacket(new Packet(1, new String[]{}));
            }
        }

        while(running){
            String input = IO.readln("\n");
            if(!input.isEmpty()){
                args = input.split(" ");
                Packet packet = new Packet(args);
                // add functionality to check user args against dictionary for command and args
                // for now args[0] is command args[1-...n] are args
                try{
                    parsePacket(packet);
                } catch (BadPacketException e) {
                    IO.println(e.getMessage());
                }
            }
        }
    }
}
