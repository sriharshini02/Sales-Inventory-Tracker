package com.tracker.model;

/**
 * Concrete class for the Staff role.
 * Corresponds to the 'Staff' class in the UML Class Diagram (Inheritance).
 */
public class Staff extends User {
    private static final long serialVersionUID = 1L;

    public Staff(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "STAFF";
    }

    // Placeholder methods corresponding to the Class Diagram (actual logic in Services)
    public void recordSale() { /* ... */ }
    public void viewInventory() { /* ... */ }
}