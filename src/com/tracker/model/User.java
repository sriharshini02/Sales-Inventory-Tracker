package com.tracker.model;

import java.io.Serializable;

/**
 * Abstract base class for all system users (ShopKeeper and Staff).
 * Corresponds to the 'User' class in the UML Class Diagram.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Attributes from Class Diagram
    private String username;
    private String password; // Stored here for simplified local authentication

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters 
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    
    /**
     * Abstract method to ensure every concrete user class defines its role.
     * This is crucial for Role-Based Access Control (RBAC).
     */
    public abstract String getRole(); 

    // Placeholder methods from Class Diagram
    public boolean login(String username, String password) {
        // Actual login logic is delegated to AuthenticationService
        return getUsername().equals(username) && getPassword().equals(password);
    }
    
    public void logout() {
        // Session invalidation logic occurs in the service/UI layer
        System.out.println(this.username + " attempting logout.");
    }
}