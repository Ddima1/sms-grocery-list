import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

public class PacketTest {
    Host host = new Host();

    @Test
    public void testPacketCreation() {
        String[] args = {"item1", "5"};
        Packet packet = new Packet(2, args);
        assertEquals(2, packet.CMD);
        assertEquals(2, packet.args.length);
    }

    @Test
    public void testPacketCommandsAll(){
        List<Item> groceries = new ArrayList<Item>();

        groceries = List.of(new Item[]{new Item("Apples", 5), new Item("Bananas", 3), new Item("Oranges", 0)});

        Packet packet2 = new Packet(2, new String[]{"Apples", "5"}); // Add item
        Packet packet3 = new Packet(2, new String[]{"Bananas", "3"});
        Packet packet4 = new Packet(2, new String[]{"Oranges", "2"});
        Packet packet8 = new Packet(2, new String[]{"Potatoes", "2"});

        Packet packet5 = new Packet(3, new String[]{"Oranges", "0"}); // Update item quantity
        Packet packet6 = new Packet(4, new String[]{"Potatoes"}); // Remove item
        Packet packet7 = new Packet(999, new String[]{});
        try{
            List<Packet> packets = new ArrayList<Packet>(List.of(new Packet[]{packet2, packet3, packet4, packet5, packet6}));
            for (Packet packet : packets) {
                host.parsePacket(packet);
            }
        } catch (BadPacketException _) {
            assertTrue(true);
        }
        Packet packet1 = new Packet(1, new String[]{}); // Get items
        host.parsePacket(packet1);
        // assertEquals(groceries, host.parsePacket(packet1));;
        assertEquals(groceries, host.getGroceries());
    }

}
