import userconnection.Client;
import userconnection.Host;

import java.util.ArrayList;

/**
 * Starts the SMS grocery-list application and configures its clients and host.
 *
 * @author Vinnie Di Maio
 * @version 1.1
 * @since 11/16/2026
 * @see userconnection.Client
 * @see userconnection.Host
 */
public class Application {
    /**
     * Collects setup values, creates the clients and host, and starts the processing threads.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        IO.println("Welcome to SMS Grocery List!\nBe sure to look at the README!");

        ArrayList<Client> clients = new ArrayList<>();
        ArrayList<Thread> clientThreads = new ArrayList<>();
        ArrayList<String> phonenumbers = new ArrayList<>();
        String sid = "";
        String username = "";
        String input;


        do{
            IO.println("Enter in phone numbers in the format: +1XXXXXXXXXX, where each X is is a digit. If you are " +
                    "non-US unfortunately this build does not work. " +
                    "Type 'NEXT' to go to the next setup step. BE SURE TO FOLLOW THE FORMAT ELSE" +
                    " AN ERROR WILL BE THROWN ON SETUP COMPLETION.");
            input = IO.readln().strip();

            if(!input.isEmpty() && !input.equals("NEXT")) {
                phonenumbers.add(input);
                IO.println("Added: " + input);
                input = "";
            }
        } while (!input.equals("NEXT"));
        input = "";

        do{
            IO.println("Enter in the TextNow SID. If you do not know where to find this see the README. " +
                    "Type 'NEXT' to go to the next setup step.");
            input = IO.readln().strip();

            if(!input.isEmpty() && !input.equals("NEXT")) {
                sid = input;
                IO.println("Set: " + input);
                input = "";
            }
        } while (!input.equals("NEXT"));
        input = "";

        do{
            IO.println("Enter in the TextNow username. If you do not know where to find this see the README. " +
                    "Type 'NEXT' to go to the next setup step.");
            input = IO.readln().strip();

            if(!input.isEmpty() && !input.equals("NEXT")) {
                username = input;
                IO.println("Set: " + input);
                input = "";
            }
        } while (!input.equals("NEXT"));
        input = "";

        for(String number : phonenumbers){
            clients.add(new Client(number, sid, username));
        }

        Host host = new Host(clients.toArray(new Client[0]));

        for(Client client : clients){
            client.setHost(host); // Cat-dog way to set allow the client to talk to the host.
            clientThreads.add(new Thread(client));
        }

        for(Thread thread : clientThreads){
            thread.start();
        }

        Thread hostThread = new Thread(host);
        hostThread.start();
    }
}
