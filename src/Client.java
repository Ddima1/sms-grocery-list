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
        sendSMS("Test Message. Hello from Indianapolis!");
    }

    private void sendSMS(String messageBody) {
        final String username = "smsgateway251@gmail.com";
        final String password = "wvxb lixk xpic duki";

        Properties emailProperties = new Properties();
        emailProperties.put("mail.smtp.host", "smtp.gmail.com");
        emailProperties.put("mail.smtp.port", "587");
        emailProperties.put("mail.smtp.auth", "true");
        emailProperties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(emailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("smsgateway251@gmail.com"));

            // Format: [Target Number]@[Carrier Gateway]
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("4023057041@tmomail.net"));
            message.setSubject("");
            message.setText(messageBody);

            Transport.send(message);
            IO.println("SMS sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
