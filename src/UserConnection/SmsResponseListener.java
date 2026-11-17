package userconnection;

import data.Packet;

public interface SmsResponseListener {
    void onDataReceived(Packet data);
}
