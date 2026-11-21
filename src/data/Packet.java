package data;

/**
 * Represents a packet message sent between the host and client.
 *
 * @author Vinnie Di Maio
 * @version 1.1
 * @since 11/04/2026
 * @see userconnection.Client
 * @see userconnection.Host
 * @see data.Item
 */
public class Packet {
    public final int ID;
    private static int nextID = 0;
    public final int CMD;
    public final String[] args;

    /**
     * Creates a packet with a command and argument.
     * Intended to be used with the host when interpreting a message.
     *
     * @param CMD command identifier
     * @param args packet arguments
     */
    public Packet(int CMD, String[] args) {
        this.ID = nextID++;
        this.CMD = CMD;
        this.args = args;
    }

    /**
     * Used to tell the host what it should tell the client class to reply.
     *
     * @param args packet arguments
     */
    public Packet(String[] args) {
        this.ID = nextID++;
        this.CMD = -1;
        this.args = args;
    }

    /**
     * Creates a response-style packet from one argument.
     * Used to tell the client the message body to reply to the user with.
     *
     * @param arg message body
     */
    public Packet(String arg) {
        this.ID = nextID++;
        this.CMD = -1;
        this.args = new String[]{arg};
    }

    /**
     * Creates a packet containing a user identifier and message.
     * Used in the host to decode which user is sending a message.
     *
     * @param userID identifier of the user
     * @param arg message body
     */
    public Packet(int userID, String arg) {
        this.ID = nextID++;
        this.CMD = -1;
        this.args = new String[]{String.valueOf(userID), arg};
    }
}
