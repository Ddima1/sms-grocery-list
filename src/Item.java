public class Item {
    private String name;
    private int quantity;
    private boolean inStock = false;

    public Item(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        updateStatus();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateStatus();
    }

    private void updateStatus() {
        if(this.quantity == 0){
            this.inStock = false;
        } else {
            this.inStock = true;
        }
    }

    public void decreaseQuantity(int amount) {
        if (amount > 0 && amount <= quantity) {
            quantity -= amount;
            updateStatus();
        }
    }

    public void increaseQuantity(int amount) {
        if (amount > 0) {
            quantity += amount;
            updateStatus();
        }
    }

    public boolean getInStock() {
        this.updateStatus();
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    @Override
    public String toString() {
        return name.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Item item)
            return quantity == item.quantity && name.equals(item.name);
        return false;
    }
}