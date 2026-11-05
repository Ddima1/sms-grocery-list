import java.util.ArrayList;
import java.util.List;

public class Host {
    private List<Item> groceries = new ArrayList<Item>();

    public List<Item> getGroceries() {
        return groceries;
    }

    public void addGroceries(Object grocery) {
        if(grocery instanceof Item e)
            this.groceries.add(e);
    }
    public void setGroceries(List<Item> groceries) {
        this.groceries = groceries;
    }
}
