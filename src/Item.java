public class Item {
    private String name;
    private int quantity;
    private boolean status = false;

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
            this.status = true;
        } else {
            this.status = false;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}