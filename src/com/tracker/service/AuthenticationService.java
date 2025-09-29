package com.tracker.service;

import com.tracker.dao.UserDAO;
import com.tracker.model.ShopKeeper;
import com.tracker.model.Staff;
import com.tracker.model.User;

import java.util.Optional;

/**
 * Handles the User Login sequence diagram logic. 
 * Corresponds to the 'Authentication' and 'Session' lifelines.
 */
public class AuthenticationService {

    // Dependency on the UserDAO for persistence
    private final UserDAO userDAO;
    
    // Represents the 'Session' lifeline: stores the currently logged-in user
    private static User activeUser; 

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        // activeUser is static, so only initialize if it's null
        if (activeUser == null) {
            userDAO.loadUsers(); // Ensure users are loaded/bootstrapped on startup
        }
    }

    /**
     * Executes the login process as per the User Login Sequence Diagram.
     * @return The authenticated User object, or null if login fails.
     */
    public User login(String username, String password) {
        
        // 1. Call 'validateCredentials' via the DAO layer
        Optional<User> foundUser = userDAO.findByUsernameAndPassword(username, password);

        if (foundUser.isPresent()) {
            // Success branch: 2. 'createSession' (by setting activeUser)
            activeUser = foundUser.get();
            System.out.println("Login successful for " + activeUser.getUsername() + " (" + activeUser.getRole() + ")");
            return activeUser;
        } else {
            // Failure branch: Login failed
            System.out.println("Login failed: Invalid credentials for user " + username);
            activeUser = null;
            return null;
        }
    }

    /**
     * Clears the active session. Corresponds to the 'logout' method.
     */
    public void logout() {
        if (activeUser != null) {
            System.out.println(activeUser.getUsername() + " logged out.");
        }
        activeUser = null;
    }

    /**
     * Getter for the current session user.
     * Other services use this to check permissions (RBAC).
     */
    public static User getActiveUser() {
        return activeUser;
    }

    /**
     * Utility method for role-based access control (RBAC).
     */
    public static boolean isActiveUserShopKeeper() {
        return activeUser != null && activeUser.getRole().equals("SHOPKEEPER");
    }
    
    public static boolean isActiveUserStaff() {
        return activeUser != null && activeUser.getRole().equals("STAFF");
    }
}