package com.tracker.dao;

import com.tracker.model.User;
import com.tracker.model.ShopKeeper;
import com.tracker.model.Staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private static final String FILE_PATH = "data/users.dat";
    private List<User> users;

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
        // Default ShopKeeper (Admin) user
        this.users.add(new ShopKeeper("admin", "admin123")); 
        // Default Staff user
        this.users.add(new Staff("staff1", "staff123"));     
        saveUsers();
    }

    /**
     * Saves the current list of users to the local file.
     */
    public void saveUsers() {
        FileStorageUtil.saveData(this.users, FILE_PATH);
    }
    
    public void add(User newUser) {
        // Ensure data is loaded
        if (this.users.isEmpty()) {
            loadUsers();
        }
        
        // Simple uniqueness check (can be expanded)
        if (users.stream().anyMatch(u -> u.getUsername().equals(newUser.getUsername()))) {
            System.err.println("Error: Username already exists.");
            return;
        }
        
        this.users.add(newUser);
        saveUsers();
    }
    
    /**
     * Corresponds to 'validateCredentials' in the Sequence Diagram.
     */
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        // Ensure data is loaded before searching
        if (this.users.isEmpty()) {
            loadUsers();
        }
        
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}