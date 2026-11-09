public class Client implements Runnable{
    private Host host = null;
    private final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    public static final String TWILIO_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    public void main(String[] args) {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();

        host = new Host();
        Thread hostThread = new Thread(host);
        hostThread.start();
    }

    @Override
    public void run() {
        sendSMS("Welcome to SMS-Grocery List! This is version 0.8, so there may be bugs!");
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
                        sendSMS(s);
                    }
                }

            }
        }
    }

    private void sendSMS(String messageBody) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(
                new PhoneNumber("+15312108112"),  // Recipient's phone number (E.164 format)
                new PhoneNumber(TWILIO_NUMBER),
                messageBody
        ).create();

        IO.println("SID: " + message.getSid());
    }

    @ResponseBody
    @PostMapping(value = "/receive-sms")
    private String receiveSMS() {
        final String host = "imap.gmail.com";
        final String username = "smsgateway251@gmail.com";
        final String password = "wvxb lixk xpic duki";

        String returnMessage = null;

        try {
            Properties properties = new Properties();
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.ssl.enable", "true");

            Session emailSession = Session.getInstance(properties);
            Store store = emailSession.getStore("imap");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            jakarta.mail.search.FlagTerm unreadTerm = new jakarta.mail.search.FlagTerm(
                    new Flags(Flags.Flag.SEEN), false
            );
            Message[] messages = inbox.search(unreadTerm);

            //IO.println("Scanning inbox for new SMS replies...");

            for (Message msg : messages) {
                String from = msg.getFrom()[0].toString();

                if (from.contains("@tmomail.net") || from.contains("@vtext.com") || from.contains("@txt.att.net")) {
                    IO.println("--- New SMS Received ---");
                    IO.println("From Mobile Number: " + from);
                    IO.println("Message Content: " + msg.getContent().toString().trim());
                    returnMessage = msg.getContent().toString().trim();

                    msg.setFlag(Flags.Flag.SEEN, true);

                }
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            //e.printStackTrace();
            throw new BadMessageException("Failed to parse message");
        }

        return returnMessage;
    }

}
