package userconnection;

import data.Packet;

/**
 * Receives packets returned to SMS clients.
 *
 * @author Vinnie Di Maio
 * @version 1.0
 * @since 11/12/2026
 * @see userconnection.Client
 * @see userconnection.Host
 */
public interface SmsResponseListener {
    /**
     * Handles a packet received from the host.
     *
     * @param data packet containing response data
     */
    void onDataReceived(Packet data);
}
