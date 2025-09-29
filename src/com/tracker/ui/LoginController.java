package com.tracker.ui;

import com.tracker.main.Main;
import com.tracker.model.User;
import com.tracker.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    // Instantiate the Service (Controller) layer
    private final AuthenticationService authService = new AuthenticationService();

    @FXML
    public void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 1. UI triggers the Service method (Executing the Sequence Diagram logic)
        User user = authService.login(username, password);

        if (user != null) {
            // Success: User is authenticated and session is created.
            try {
                // Load the main application view
                Main.showMainView(); 
            } catch (IOException e) {
                messageLabel.setText("Login Success, but failed to load main app: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Failure: Credentials invalid.
            messageLabel.setText("Invalid Username or Password.");
            passwordField.clear(); // Clear password field for security
        }
    }
}