package com.tracker.model;

/**
 * Concrete class for the ShopKeeper role.
 * Corresponds to the 'ShopKeeper' class in the UML Class Diagram (Inheritance).
 */
public class ShopKeeper extends User {
    private static final long serialVersionUID = 1L;

    public ShopKeeper(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "SHOPKEEPER";
    }

    // Placeholder methods corresponding to the Class Diagram (actual logic in Services)
    public void addProduct(Product p) { /* ... */ }
    public void editProduct(Product p) { /* ... */ }
    public void removeProduct(String productId) { /* ... */ }
    public void generateReports() { /* ... */ }
}