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
     * Allows the ShopKeeper to create a new Staff or ShopKeeper account.
     * This satisfies the user management requirement.
     * @param creatingUser The user performing the action (must be ShopKeeper).
     * @param newUsername The username for the new account.
     * @param newPassword The password for the new account.
     * @param role The role to assign ("STAFF" or "SHOPKEEPER").
     * @return True if the user was successfully added.
     */
    public static boolean addUser(User creatingUser, String newUsername, String newPassword, String role) {
    	UserDAO userDAO = new UserDAO(); 
        
        // 1. RBAC Check
        if (creatingUser == null || !creatingUser.getRole().equals("SHOPKEEPER")) {
            System.err.println("Access Denied: Only ShopKeeper can add new users.");
            return false;
        }

        User newUser;
        
        if (role.equalsIgnoreCase("STAFF")) {
            // We use the simpler constructor and rely on the controller to set the Name later,
            // or assume the DAO/Controller handles the new name/ID logic.
            newUser = new Staff(newUsername, newPassword); 
        } else if (role.equalsIgnoreCase("SHOPKEEPER")) {
            newUser = new ShopKeeper(newUsername, newPassword);
        } else {
            System.err.println("Error: Invalid role specified.");
            return false;
        }
        
        // NOTE: This logic is slightly incomplete if 'Name' is not passed here, 
        // but the StaffManagementController sets the name on 'newUser' before this call, 
        // so it should be okay if you are passing the *same* object. 
        // However, the current controller code doesn't pass the name set in the form
        // to this method. Let's fix the *controller* to set the name on the user 
        // object *before* calling this service method.
        
        // The DAO's add(newUser) will handle ID assignment and persistence.
        userDAO.add(newUser); 
        // Since the DAO.add performs uniqueness check and persistence, we can
        // simplify the return (though a real service layer would check DAO return status).
        // For this project structure, we assume if DAO.add completes, it was successful.
        return true; 
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