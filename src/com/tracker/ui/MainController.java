package com.tracker.ui;

import com.tracker.main.Main;
import com.tracker.model.User;
import com.tracker.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML private Label userLabel;
    @FXML private VBox contentArea;
    @FXML private Button productManagementBtn;
    @FXML private Button recordPurchaseBtn;
    @FXML private Button salesHistoryBtn;
    @FXML private Button reportsBtn;
    @FXML private Label shopKeeperHeader;

    private User activeUser;

    @FXML
    public void initialize() {
        // Called after FXML elements are loaded

        // 1. Get the current active user from the service (Session)
        activeUser = AuthenticationService.getActiveUser();

        if (activeUser == null) {
            // Should not happen if login worked, but handle defensively
            userLabel.setText("User: UNAUTHORIZED");
            return;
        }

        userLabel.setText("User: " + activeUser.getUsername() + " (" + activeUser.getRole() + ")");

        // 2. Implement Role-Based Access Control (RBAC)
        if (!AuthenticationService.isActiveUserShopKeeper()) {
            // Hide ShopKeeper specific tools for Staff users
            productManagementBtn.setManaged(false);
            productManagementBtn.setVisible(false);
            recordPurchaseBtn.setManaged(false);
            recordPurchaseBtn.setVisible(false);
            salesHistoryBtn.setManaged(false);
            salesHistoryBtn.setVisible(false);
            reportsBtn.setManaged(false);
            reportsBtn.setVisible(false);
            shopKeeperHeader.setManaged(false);
            shopKeeperHeader.setVisible(false);
        }
        
        // Show default view (Inventory) upon load
        showInventoryView(); 
    }

    private void loadView(String fxmlPath) {
        try {
            // Clear content and load new view
            contentArea.getChildren().clear();
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();
            contentArea.getChildren().add(new Label("Error loading view."));
        }
    }

    // --- Navigation Handlers ---

    @FXML
    public void showInventoryView() {
        // Implements View Current Stock Use Case (available to both roles)
        loadView("/com/tracker/ui/InventoryView.fxml");
    }

    @FXML
    public void showSalesRegisterView() {
        // Implements Record Sale Use Case (available to both roles)
        loadView("/com/tracker/ui/SalesRegisterView.fxml");
    }

    @FXML
    public void showProductManagementView() {
        // Implements Add/Edit/Remove Product Use Cases (ShopKeeper only)
        loadView("/com/tracker/ui/ProductManagementView.fxml");
    }
    
    @FXML
    public void showPurchaseView() {
        // Implements Record Purchase Use Case (ShopKeeper only)
        loadView("/com/tracker/ui/PurchaseView.fxml");
    }
    
    @FXML
    public void showSalesHistoryView() {
        // Implements View Sales History Use Case (ShopKeeper only)
        loadView("/com/tracker/ui/SalesHistoryView.fxml");
    }

    @FXML
    public void showReportsView() {
        // Implements Generate Reports Use Case (ShopKeeper only)
        loadView("/com/tracker/ui/ReportsView.fxml");
    }
    
    @FXML
    public void handleLogout() throws IOException {
        AuthenticationService authService = new AuthenticationService();
        authService.logout();
        Main.showLoginView();
    }
}