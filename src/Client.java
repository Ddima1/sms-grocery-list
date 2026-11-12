import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Client implements Runnable{
    private static final Host host = new Host();
    private final String ENDPOINT = "http://127.0.0.1:5000";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String recipient = "+15312018112";
    /* GitIgnore:
      - 15159018781
      - 14023057041
      - 15312018112
     */
    public static void main(String[] args) {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();

        Thread hostThread = new Thread(host);
        hostThread.start();
    }

    @Override
    public void run() {
        try {
            sendSMS("Welcome to SMS-Grocery List! This is version 0.9, so there may be bugs!");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
            // e.printStackTrace();
        }
        if(host != null){
            while(host.getStaus()){
                try{
                    String receivedMessage = receiveSMS();
                    if(!receivedMessage.isEmpty()){
                        host.parseText(receivedMessage);
                        receivedMessage = "";
                    }
                } catch (Exception e){
                    String s = "";
                    try{
                        s = e.getMessage();
                    } catch (NullPointerException _){}
                    if(s != null && !s.isEmpty()){
                        try {
                            sendSMS(s);
                        } catch (IOException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }

            }
        }
    }

    private void sendSMS(String messageBody) throws IOException, InterruptedException {
        int maxRetries = 5;
        int retries = 0;
        boolean sent = false;

        messageBody = messageBody.replace("\"", "\\\"");



        String jsonPayload = String.format(
                "{\"to\": \"%s\", \"body\": \"%s\"}",
                recipient,
                messageBody
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + "/send"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        while(!sent && (retries < maxRetries)){
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                IO.println("Python Gateway Response Code: " + response.statusCode());
                IO.println("Python Gateway Response Body: " + response.body());

                if(response.statusCode() == 200) { // 200 = Success
                    sent = true;
                } else {
                    throw new SidecarException("Python gateway responded with error status: " + response.statusCode());
                }

            } catch (ConnectException e) {
                retries++;
                IO.println("Connection failed. Retry count: " + retries + " of " + maxRetries);
                Thread.sleep(2000);

                if(retries >= maxRetries){
                    throw new SidecarException("Failed to connect to python script! Attempts: "+maxRetries);
                }
            } catch (Exception e) {
                throw new SidecarException("Error parsing SMS to Python: " + e.getMessage());
            }
        }
    }

        return "";
    }

}
