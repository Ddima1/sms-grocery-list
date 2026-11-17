import userconnection.Client;
import userconnection.Host;

public class Application {
    public static void main(String[] args) {
        Client client = new Client();
        Host host = new Host(client);
        Client.setHost(host); // Dog-cat way to set allow the client to talk to the host.

        Thread clientThread = new Thread(client);
        clientThread.start();

        Thread hostThread = new Thread(host);
        hostThread.start();
    }
}
