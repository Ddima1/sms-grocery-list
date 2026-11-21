package data;

/**
 * Represents a grocery item and its quantity.
 *
 * @author Vinnie Di Maio
 * @version 1.0
 * @since 11/04/2026
 * @see userconnection.Client
 * @see userconnection.Host
 * @see data.Packet
 */
public class Item {
    private String name;
    private int quantity;
    private boolean inStock = false;

    /**
     * Creates an item with a name and quantity.
     *
     * @param name item name
     * @param quantity item quantity
     */
    public Item(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        updateStatus();
    }

    /**
     * Gets the item's name.
     *
     * @return the item name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the item's name.
     *
     * @param name new item name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the item's quantity.
     *
     * @return the item quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the item's quantity and updates its stock status.
     *
     * @param quantity new item quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateStatus();
    }

    /**
     * Updates whether the item is in stock based on its quantity.
     */
    private void updateStatus() {
        if(this.quantity == 0){
            this.inStock = false;
        } else {
            this.inStock = true;
        }
    }

    /**
     * Decreases the quantity by a valid positive amount.
     *
     * @param amount amount to remove
     */
    public void decreaseQuantity(int amount) {
        if (amount > 0 && amount <= quantity) {
            quantity -= amount;
            updateStatus();
        }
    }

    /**
     * Increases the quantity by a valid positive amount.
     *
     * @param amount amount to add
     */
    public void increaseQuantity(int amount) {
        if (amount > 0) {
            quantity += amount;
            updateStatus();
        }
    }

    /**
     * Gets whether the item is in stock.
     *
     * @return true if the item has a positive quantity
     */
    public boolean getInStock() {
        this.updateStatus();
        return inStock;
    }

    /**
     * Sets whether the item is in stock.
     *
     * @param inStock stock status
     */
    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    /**
     * Returns the lowercase item name.
     *
     * @return the lowercase item name
     */
    @Override
    public String toString() {
        return name.toLowerCase();
    }

    /**
     * Compares this item with another item by name and quantity.
     *
     * @param o object to compare
     * @return true if both items have the same name and quantity, else false
     *
     *
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Item item)
            return quantity == item.quantity && name.equals(item.name);
        return false;
    }
}