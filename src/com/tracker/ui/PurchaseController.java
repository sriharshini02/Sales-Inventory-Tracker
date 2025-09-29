package com.tracker.ui;

import com.tracker.service.AuthenticationService;
import com.tracker.service.InventoryService;
import com.tracker.service.PurchaseService;
import com.tracker.model.Product; // Import Product
import com.tracker.dao.ProductDAO; 
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.Optional; // Import Optional

public class PurchaseController {

    // REMOVE @FXML private TextField purchaseIDField;

    @FXML private TextField purchaseNameField; 
    @FXML private TextField quantityField;
    @FXML private TextField costPriceField;
    @FXML private TextField supplierNameField;
    @FXML private Label messageLabel;
    
    // NOTE: ProductDAO is only used for the helper method; it's better to get the 
    // functionality via InventoryService if possible, but we'll use it as defined for now.
    private final ProductDAO productDAO = new ProductDAO(); 
    
    private final InventoryService inventoryService = new InventoryService();
    private final PurchaseService purchaseService = new PurchaseService(inventoryService);
    
    // The constructor is now cleaner (Java will auto-initialize fields)
    // public PurchaseController(){ /* this.productDAO = new ProductDAO(); */ } 

    // Helper method should be in InventoryService, but included here for compilation:
    public Optional<Product> getProductByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        // Calls the DAO (ensure ProductDAO has the getAllProducts method implemented)
        return this.productDAO.getAllProducts().stream() 
                .filter(p -> p.getName().trim().equalsIgnoreCase(name.trim()))
                .findFirst();
    }
    
    @FXML
    public void handleRecordPurchase() {
        // --- LOGIC CHANGE STARTS HERE ---
        String productName = purchaseNameField.getText().trim(); // Use name from UI
        String productId; // Placeholder for the ID we will look up
        
        int quantity;
        double costPrice;
        String supplierName = supplierNameField.getText().trim();
        
        // 1. INPUT VALIDATION & PARSING
        try {
            // ... (Quantity and Cost Price parsing logic remains) ...
            quantity = Integer.parseInt(quantityField.getText());
            costPrice = Double.parseDouble(costPriceField.getText());
            if (quantity <= 0 || costPrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            messageLabel.setText("Error: Quantity and Cost Price must be positive numbers.");
            return;
        }
        
        if (productName.isEmpty()) {
            messageLabel.setText("Error: Product Name is required.");
            return;
        }
        if (supplierName.isEmpty()) {
            messageLabel.setText("Error: Supplier Name is required.");
            return;
        }

        // 2. PRODUCT LOOKUP (Find ID by Name)
        Optional<Product> productOpt = getProductByName(productName);
        
        if (productOpt.isEmpty()) {
            messageLabel.setText("Error: Product not found by name: " + productName);
            return;
        }
        
        productId = productOpt.get().getProductID(); 
        // --- LOGIC CHANGE ENDS HERE ---

        // 3. CALL SERVICE
        String result = purchaseService.recordPurchase(
            AuthenticationService.getActiveUser(), 
            productId, // Pass the found ID
            quantity, 
            costPrice, 
            supplierName
        );

        // 4. PROCESS RESULT
        if (result.startsWith("Error")) {
            messageLabel.setText(result);
        } else {
            messageLabel.setText(result);
            // Clear inputs upon success
            purchaseNameField.clear(); // Clear name field
            quantityField.clear();
            costPriceField.clear();
            supplierNameField.clear();
        }
    }
}