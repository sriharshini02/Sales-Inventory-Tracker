package com.tracker.ui;

import com.tracker.dao.UserDAO;
import com.tracker.model.ShopKeeper;
import com.tracker.model.Staff;
import com.tracker.model.User;
import com.tracker.service.AuthenticationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.Optional;

public class StaffManagementController {

    // FXML elements
    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordLabel;
    @FXML private Button actionButton;
    @FXML private Button resetPasswordButton;
    @FXML private Label messageLabel;
    @FXML private TableView<User> staffTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Void> actionsColumn; 

    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> userList;
    private User editingUser = null; 

    // --- Utility Methods (replacing AlertUtil) ---

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
    
    private Optional<String> showPasswordResetDialog(String username) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password for " + username);
        dialog.setHeaderText("Enter a new password for " + username + ".");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (min 3 chars)");
        
        grid.add(new Label("New Password:"), 0, 0);
        grid.add(passwordField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // --- Controller Logic ---

    @FXML
    public void initialize() {
        User currentUser = AuthenticationService.getActiveUser();
        if (currentUser == null || !currentUser.getRole().equals("SHOPKEEPER")) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only ShopKeepers are authorized to manage staff accounts.");
            return; 
        }

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        roleComboBox.setItems(FXCollections.observableArrayList(Arrays.asList("SHOPKEEPER", "STAFF")));
        
        loadStaffTable();
        handleClearForm(null);
    }

    private void loadStaffTable() {
        userList = FXCollections.observableArrayList(userDAO.getAllUsers());
        staffTable.setItems(userList);
    }

    @FXML
    private void handleSaveOrUpdate(ActionEvent event) {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();

        if (name.isEmpty() || username.isEmpty() || role == null) {
            messageLabel.setText("Please fill in all required fields (Name, Username, Role).");
            return;
        }
        
        if (editingUser == null) {
            // --- ADD NEW STAFF MODE ---
            if (password.isEmpty() || password.length() < 3) {
                messageLabel.setText("Password is required for new staff and must be at least 3 characters.");
                return;
            }
            
            User newUser;
            if (role.equalsIgnoreCase("SHOPKEEPER")) {
                // ID and Name are set in DAO.add
                newUser = new ShopKeeper(username, password);
            } else {
                newUser = new Staff(username, password);
            }
            newUser.setName(name); 
            
            if (AuthenticationService.addUser(AuthenticationService.getActiveUser(), newUser.getUsername(), newUser.getPassword(), newUser.getRole())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "New staff member '" + name + "' added successfully!");
                handleClearForm(null);
                loadStaffTable();
            } else {
                messageLabel.setText("Failed to add user. Username may already exist.");
            }
        } else {
            // --- UPDATE STAFF MODE ---
            User activeUser = AuthenticationService.getActiveUser();
            if (editingUser.getId() == activeUser.getId() && !role.equals("SHOPKEEPER")) {
                 messageLabel.setText("You cannot demote your own active account role.");
                 return;
            }
            
            // Create the updated user object
            User updatedUser;
            if (role.equalsIgnoreCase("SHOPKEEPER")) {
                updatedUser = new ShopKeeper(editingUser.getId(), editingUser.getUsername(), editingUser.getPassword(), name);
            } else {
                updatedUser = new Staff(editingUser.getId(), editingUser.getUsername(), editingUser.getPassword(), name);
            }

            if (userDAO.update(updatedUser)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Staff member '" + name + "' updated successfully!");
                handleClearForm(null);
                loadStaffTable();
            } else {
                messageLabel.setText("Failed to update user.");
            }
        }
    }
    
    @FXML
    private void handleClearForm(ActionEvent event) {
        editingUser = null;
        nameField.clear();
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        messageLabel.setText("");
        
        formTitleLabel.setText("Add New Staff Member");
        actionButton.setText("Add Staff");
        usernameField.setDisable(false); 
        passwordField.setVisible(true);
        passwordLabel.setVisible(true);
        resetPasswordButton.setVisible(false);
    }

    @FXML
    private void handleEditUser(ActionEvent event) {
        User selectedUser = staffTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            messageLabel.setText("Please select a staff member from the table to edit.");
            return;
        }

        editingUser = selectedUser;

        formTitleLabel.setText("Edit Staff Member (ID: " + selectedUser.getId() + ")");
        nameField.setText(selectedUser.getName());
        usernameField.setText(selectedUser.getUsername());
        usernameField.setDisable(true); 
        roleComboBox.setValue(selectedUser.getRole());
        
        passwordField.setVisible(false);
        passwordLabel.setVisible(false);
        resetPasswordButton.setVisible(true);
        
        actionButton.setText("Update Staff");
        messageLabel.setText("Editing user: " + selectedUser.getName() + ".");
    }
    
    @FXML
    private void handleResetPassword(ActionEvent event) {
        if (editingUser == null) {
            messageLabel.setText("Please select a user and switch to Edit mode first.");
            return;
        }
        
        Optional<String> result = showPasswordResetDialog(editingUser.getUsername());

        result.ifPresent(newPassword -> {
            if (newPassword.length() < 3) {
                showAlert(Alert.AlertType.ERROR, "Error", "New password must be at least 3 characters long.");
                return;
            }
            if (userDAO.updatePassword(editingUser.getId(), newPassword)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password for user '" + editingUser.getUsername() + "' has been successfully reset.");
                loadStaffTable(); 
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset password.");
            }
        });
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = staffTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            messageLabel.setText("Please select a staff member to delete.");
            return;
        }
        
        User activeUser = AuthenticationService.getActiveUser();
        if (selectedUser.getId() == activeUser.getId()) {
            showAlert(Alert.AlertType.ERROR, "Action Denied", "You cannot delete your own active account.");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
            "Confirm Delete", 
            "Are you sure you want to permanently delete the account for '" + selectedUser.getName() + "'?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.delete(selectedUser.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Staff member '" + selectedUser.getName() + "' deleted successfully.");
                loadStaffTable(); 
                handleClearForm(null); 
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete staff member.");
            }
        }
    }
}