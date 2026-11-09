public class Client implements Runnable{
    private Host host = null;

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

    }

    private String receiveSMS() {

        return "";
    }

}
