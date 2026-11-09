import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class Client implements Runnable{
    public void main(String[] args) {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();

        Host host = new Host();
        Thread hostThread = new Thread(host);
        hostThread.start();
    }

    @Override
    public void run() {
        // Client implementation
    }
}
