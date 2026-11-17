package userconnection;

import exceptions.SidecarException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Client implements Runnable, SmsResponseListener{
    private static Host host = null;
    private final String ENDPOINT = "http://127.0.0.1:5000";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String recipient = "+15312108112";

    public static void setHost(Host host) {
        Client.host = host;
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

    private String receiveSMS() {
        try{
            Thread.sleep(5000); // prevents overworking the thread and Python client
        } catch (InterruptedException _) {}

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + "/get-next-sms")) // calls the Python script
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

    @Override
    public void onDataReceived(String data) {
        try{
            sendSMS(data);
        } catch (Exception e){
            IO.println(e.getMessage());
        }
    }

}
