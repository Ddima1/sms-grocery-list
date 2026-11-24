package userconnection;

import data.Packet;
import exceptions.SidecarException;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;

/**
 * Manages SMS polling and sending for one recipient through a Python sidecar.
 *
 * @author Vinnie Di Maio
 * @version 1.1
 * @since 11/05/2026
 * @see userconnection.Host
 */
public class Client implements Runnable, SmsResponseListener {
    private static int clients = 0;
    private static final int BASE_PORT = 5000;
    private final int clientID;

    private Host host;
    private final int port;
    private final String endpoint;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String recipient;
    private final String sid;
    private final String username;

    /**
     * Creates a client and launches its gateway sidecar.
     *
     * @param recipient recipient phone number
     * @param sid TextNow session identifier
     * @param username TextNow account username
     * @throws exceptions.SidecarException if the sidecar cannot be started
     */
    public Client(String recipient, String sid, String username) {
        this.clientID = clients;
        this.port = BASE_PORT + clientID;
        this.endpoint = "http://127.0.0.1:" + port;
        this.recipient = recipient;
        this.sid = sid;
        this.username = username;
        launchPythonSidecar();
        clients++;
    }

    /**
     * Associates this client with its host.
     *
     * @param host host that processes received messages
     */
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * Sends welcome messages and continuously polls for incoming SMS messages every 5000ms.
     */
    @Override
    public void run() {
        try {
            sendSMS("Welcome to SMS-Grocery List! This is version 1.0.");
            sendSMS("If you have any issues or bugs contact your Server Administrator.");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
            // e.printStackTrace();
        }
        if(host != null){
            while(host.getStaus()){
                try{
                    String receivedMessage = receiveSMS();
                    if(!receivedMessage.isEmpty()){
                        host.parseText(new Packet(clientID, receivedMessage));
                        receivedMessage = null;
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

    /**
     * Sends an SMS message through the local Python gateway/sidecar.
     *
     * @param messageBody text to send
     * @throws IOException if the HTTP request cannot be sent
     * @throws InterruptedException if the sending thread is interrupted
     * @throws exceptions.SidecarException if the gateway returns an error or cannot be reached
     */
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
                .uri(URI.create(endpoint + "/send"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        while(!sent && retries < maxRetries){
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

    /**
     * Polls the gateway for the next SMS message.
     *
     * @return the received message body, or an empty string
     * @throws exceptions.SidecarException if polling or JSON processing fails
     */
    private String receiveSMS() {
        try{
            Thread.sleep(5000); // prevents overworking the thread and Python client
        } catch (InterruptedException _) {}

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/get-next-sms")) // calls the Python script
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SidecarException("Python gateway responded with error status: " + response.statusCode());
            }
            return extractMessageBody(response.body());
        } catch (IOException | InterruptedException e) {
            throw new SidecarException("Failed to poll python script: " + e.getMessage());
        }
    }

    /**
     * Extracts a message body from a gateway JSON response.
     *
     * @param json gateway response JSON
     * @return extracted message body
     */
    private String extractMessageBody(String json) {
        int key = json.indexOf("\"body\"");
        if (key < 0)
            return "";

        int start = json.indexOf('"', json.indexOf(':', key) + 1);
        if (start < 0)
            return "";

        StringBuilder body = new StringBuilder();

        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') break; // end of the string value
            if (c == '\\' && i + 1 < json.length()) { // JSON escape
                char n = json.charAt(++i);
                switch (n) {
                    case 'n' -> body.append('\n'); // new line
                    case 't' -> body.append('\t'); // tab
                    case 'r' -> body.append('\r'); // return
                    case 'u' -> { // non-ASCII
                        body.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    default -> body.append(n); // \" and \\ and \/
                }
            } else {
                body.append(c);
            }
        }

        return body.toString();
    }

    /**
     * Sends data received from the host to the SMS recipient.
     *
     * @param data response packet to send
     */
    @Override
    public void onDataReceived(Packet data) {
        try{
            sendSMS(data.args[0]);
        } catch (Exception e){
            IO.println(e.getMessage());
        }
    }

    /**
     * Starts the Python gateway process for this client.
     *
     * @throws exceptions.SidecarException if the gateway cannot be located or started
     */
    private void launchPythonSidecar() {
        // Directory is found this way since putting the directory in straight caused bugs
        String directory = System.getProperty("user.dir");
        String pythonPath = Paths.get(System.getProperty("user.dir"), "src", "main",
                "userconnection", "textnow_gateway", "textnowGateway.py").toString();

        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonPath);
        processBuilder.environment().put("RECIPIENT_PHONE", recipient);
        processBuilder.environment().put("TEXTNOW_SID", sid);
        processBuilder.environment().put("TEXTNOW_USERNAME", username);
        processBuilder.environment().put("PORT", Integer.toString(port));
        try {
            processBuilder.inheritIO();
            Process pythonProcess = processBuilder.start();

            // Ensures python is killed at the same time of java process
            Runtime.getRuntime().addShutdownHook(new Thread(pythonProcess::destroy));

            Thread.sleep(2500); // Python is slow to connect :/

        } catch (IOException e){
            throw new SidecarException("Fatal Error: Unable to locate python file: textnowGateway.py, ensure the file directory is correct!");
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } catch (Exception e){
            throw new SidecarException("Other error with Sidecar.");
        }

    }

}
