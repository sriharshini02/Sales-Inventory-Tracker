package com.tracker.ui;

import com.tracker.service.AuthenticationService;
import com.tracker.service.InventoryService;
import com.tracker.service.PurchaseService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PurchaseController {

    @FXML private TextField purchaseIDField;
    @FXML private TextField quantityField;
    @FXML private TextField costPriceField;
    @FXML private TextField supplierNameField;
    @FXML private Label messageLabel;
    
    // Services need to be instantiated
    private final InventoryService inventoryService = new InventoryService();
    private final PurchaseService purchaseService = new PurchaseService(inventoryService);

    @FXML
    public void handleRecordPurchase() {
        String productId = purchaseIDField.getText().trim();
        int quantity;
        double costPrice;
        String supplierName = supplierNameField.getText().trim();
        
        try {
            quantity = Integer.parseInt(quantityField.getText());
            costPrice = Double.parseDouble(costPriceField.getText());
            if (quantity <= 0 || costPrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            messageLabel.setText("Error: Quantity and Cost Price must be positive numbers.");
            return;
        }
        
        if (supplierName.isEmpty()) {
            messageLabel.setText("Error: Supplier Name is required.");
            return;
        }

        // Call the service method to execute the purchase logic
        String result = purchaseService.recordPurchase(
            AuthenticationService.getActiveUser(), 
            productId, 
            quantity, 
            costPrice, 
            supplierName
        );

        // Process result
        if (result.startsWith("Error")) {
            messageLabel.setText(result);
        } else {
            messageLabel.setText(result);
            // Clear inputs upon success
            purchaseIDField.clear();
            quantityField.clear();
            costPriceField.clear();
            supplierNameField.clear();
        }
    }
}