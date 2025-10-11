package com.tracker.dao;

import com.tracker.model.User;
import com.tracker.model.ShopKeeper;
import com.tracker.model.Staff;

import com.tracker.model.User;
import com.tracker.model.ShopKeeper;
import com.tracker.model.Staff;
import com.tracker.dao.FileStorageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private static final String FILE_PATH = "data/users.dat";
    private List<User> users;
    private int nextUserId = 1;

    public UserDAO() {
        this.users = new ArrayList<>();
    }
    
    /**
     * Loads user data from file and bootstraps initial users if the file is empty.
     */
    public void loadUsers() {
        this.users = FileStorageUtil.loadData(FILE_PATH);
        
        // Ensure initial data exists if the file is empty (Bootstrap)
        if (this.users.isEmpty()) {
            bootstrapInitialUsers();
        }
    }

    private void bootstrapInitialUsers() {
        System.out.println("Bootstrapping initial users...");
        // Assign explicit IDs here for bootstrap, starting from 1
        this.users.add(new ShopKeeper(1, "admin", "admin123", "Admin User")); 
        this.users.add(new Staff(2, "staff1", "staff123", "Default Staff")); 
        
        // Ensure ID counter starts after bootstrap
        this.nextUserId = 3; 
        saveUsers();
    }

    /**
     * Saves the current list of users to the local file.
     */
    public void saveUsers() {
        FileStorageUtil.saveData(this.users, FILE_PATH);
    }
    
    public void add(User newUser) {
        if (this.users.isEmpty()) {
            loadUsers();
        }
        
        if (users.stream().anyMatch(u -> u.getUsername().equals(newUser.getUsername()))) {
            System.err.println("Error: Username already exists.");
            return;
        }
        
        // Assign unique ID before adding
        newUser.setId(nextUserId++); 
        
        this.users.add(newUser);
        saveUsers();
    }
    
    /**
     * Corresponds to 'validateCredentials' in the Sequence Diagram.
     */
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        if (this.users.isEmpty()) {
            loadUsers();
        }
        
        return users.stream()
                // NOTE: Assuming User model now has a getName() method
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();
    }
    public List<User> getAllUsers() {
        if (this.users.isEmpty()) {
            loadUsers();
        }
        return new ArrayList<>(users);
    }
    
    public boolean update(User updatedUser) {
        if (this.users.isEmpty()) {
            loadUsers();
        }
        
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updatedUser.getId()) {
                users.set(i, updatedUser);
                saveUsers();
                return true;
            }
        }
        System.err.println("Error: Could not find user with ID " + updatedUser.getId() + " to update.");
        return false;
    }
    
    /**
     * Deletes a user from the list and saves.
     */
    public boolean delete(int userId) {
        if (this.users.isEmpty()) {
            loadUsers();
        }
        
        boolean removed = users.removeIf(u -> u.getId() == userId);
        if (removed) {
            saveUsers();
        }
        return removed;
    }
    
    /**
     * Updates a user's password and saves.
     */
    public boolean updatePassword(int userId, String newPassword) {
        if (this.users.isEmpty()) {
            loadUsers();
        }
        
        for (int i = 0; i < users.size(); i++) {
            User existingUser = users.get(i);
            if (existingUser.getId() == userId) {
                // Create a new instance (ShopKeeper/Staff) with the updated password 
                // to maintain the correct class type during serialization.
                User updatedUser;
                if (existingUser instanceof ShopKeeper) {
                    // Assuming ShopKeeper and Staff constructors now accept ID, Username, Password, Name
                    updatedUser = new ShopKeeper(userId, existingUser.getUsername(), newPassword, existingUser.getName());
                } else { // Staff
                    updatedUser = new Staff(userId, existingUser.getUsername(), newPassword, existingUser.getName());
                }
                
                users.set(i, updatedUser);
                saveUsers();
                return true;
            }
        }
        return false;
    }
}