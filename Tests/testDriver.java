import java.util.ArrayList;
import java.util.List;

public class testDriver {
    public static void main(String[] args) {
        Host host = new Host();
        List<Item> groceries = new ArrayList<Item>();

        groceries.add(new Item("Apples", 5));
        groceries.add(new Item("Bananas", 3));
        groceries.add(new Item("Oranges", 0));

        host.setGroceries(groceries);

        for (Item item : host.getGroceries()) {
            IO.println("Item: " + item.getName() + ", Quantity: " + item.getQuantity() + ", Status: " + item.getInStock());
        }

        Thread hostThread = new Thread(host);
        hostThread.start();
    }
}
