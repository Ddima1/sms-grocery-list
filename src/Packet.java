public class Packet {
    public final int ID;
    private static int nextID = 0;
    public final int CMD;
    public final String[] args;

    public Packet(int CMD, String[] args) {
        this.ID = nextID++;
        this.CMD = CMD;
        this.args = args;
    }

    public Packet(String[] args) {
        this.ID = nextID++;
        this.CMD = -1;
        this.args = args;
    }
}
