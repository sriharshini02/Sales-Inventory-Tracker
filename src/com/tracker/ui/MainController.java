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
    @FXML private Button staffManagementBtn; // <--- NEW FXML ID

    private User activeUser;

    @FXML
    public void initialize() {
        activeUser = AuthenticationService.getActiveUser();

        if (activeUser == null) {
            userLabel.setText("User: UNAUTHORIZED");
            return;
        }

        userLabel.setText("User: " + activeUser.getName() + " (" + activeUser.getRole() + ")"); // Use getName() if available

        // 2. Implement Role-Based Access Control (RBAC)
        boolean isShopKeeper = AuthenticationService.isActiveUserShopKeeper();

        // List of all ShopKeeper controls (including the new one)
        Button[] shopKeeperButtons = {
            productManagementBtn, recordPurchaseBtn, salesHistoryBtn, reportsBtn, staffManagementBtn // <--- NEW BUTTON
        };

        for (Button btn : shopKeeperButtons) {
            if (btn != null) { // Check for null in case FXML loading failed partially
                btn.setManaged(isShopKeeper);
                btn.setVisible(isShopKeeper);
            }
        }

        if (shopKeeperHeader != null) {
            shopKeeperHeader.setManaged(isShopKeeper);
            shopKeeperHeader.setVisible(isShopKeeper);
        }
        
        // Show default view (Inventory) upon load
        showInventoryView(); 
    }

    private void loadView(String fxmlPath) {
        try {
            contentArea.getChildren().clear();
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS); // Ensure content scales
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();
            contentArea.getChildren().add(new Label("Error loading view: " + e.getMessage()));
        }
    }

    // --- Navigation Handlers ---

    @FXML
    public void showInventoryView() {
        loadView("/com/tracker/ui/InventoryView.fxml");
    }

    @FXML
    public void showSalesRegisterView() {
        loadView("/com/tracker/ui/SalesRegisterView.fxml");
    }

    @FXML
    public void showProductManagementView() {
        loadView("/com/tracker/ui/ProductManagementView.fxml");
    }
    
    @FXML
    public void showPurchaseView() {
        loadView("/com/tracker/ui/PurchaseView.fxml");
    }
    
    @FXML
    public void showSalesHistoryView() {
        loadView("/com/tracker/ui/SalesHistoryView.fxml");
    }

    @FXML
    public void showReportsView() {
        loadView("/com/tracker/ui/ReportsView.fxml");
    }
    
    /**
     * Handler for the new "Manage Staff" button.
     */
    @FXML
    public void showStaffManagementView() {
        loadView("/com/tracker/ui/StaffManagementView.fxml");
    }

    @FXML
    public void handleLogout() throws IOException {
        AuthenticationService authService = new AuthenticationService();
        authService.logout();
        Main.showLoginView();
    }
}