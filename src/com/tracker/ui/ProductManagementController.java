package com.tracker.ui;

import com.tracker.model.Product;
import com.tracker.service.AuthenticationService;
import com.tracker.service.InventoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.UUID;

public class ProductManagementController {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField costPriceField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField stockField;
    @FXML private Label messageLabel;
    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, String> colID;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colSell;
    @FXML private TableColumn<Product, Integer> colStock;

    private final InventoryService inventoryService = new InventoryService();

    @FXML
    public void initialize() {
        setupTable();
        loadProducts();
        
        // Listener to load selected product details into the form
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }

    private void setupTable() {
        colID.setCellValueFactory(new PropertyValueFactory<>("productID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSell.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
    }

    private void loadProducts() {
        inventoryTable.setItems(FXCollections.observableArrayList(inventoryService.viewCurrentStock()));
    }
    
    private void populateForm(Product p) {
        idField.setText(p.getProductID());
        nameField.setText(p.getName());
        categoryField.setText(p.getCategory());
        costPriceField.setText(String.valueOf(p.getCostPrice()));
        sellingPriceField.setText(String.valueOf(p.getSellingPrice()));
        stockField.setText("0"); // Stock change is usually zero unless adding purchase
        messageLabel.setText("");
    }
    
    @FXML
    public void handleClearForm() {
        idField.setText("");
        nameField.clear();
        categoryField.clear();
        costPriceField.clear();
        sellingPriceField.clear();
        stockField.clear();
        messageLabel.setText("Form cleared.");
        inventoryTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleSaveProduct() {
        try {
            String id = idField.getText().isEmpty() ? UUID.randomUUID().toString().substring(0, 8) : idField.getText();
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            double cost = Double.parseDouble(costPriceField.getText());
            double sell = Double.parseDouble(sellingPriceField.getText());
            int stockChange = stockField.getText().isEmpty() ? 0 : Integer.parseInt(stockField.getText());

            if (name.isEmpty() || category.isEmpty()) {
                messageLabel.setText("Error: Name and Category are required.");
                return;
            }

            Product newProduct = new Product(id, name, category, cost, sell, 0); // Initial stock set to 0 here
            
            // Execute the Add Product sequence diagram logic (Service handles whether it's new or update)
            boolean success = inventoryService.addOrUpdateProduct(
                AuthenticationService.getActiveUser(), 
                newProduct, 
                stockChange // Use stockChange as the amount to add/update
            );

            if (success) {
                messageLabel.setText("Product " + name + " saved successfully!");
                loadProducts(); // Refresh table
                handleClearForm();
            } else {
                messageLabel.setText("Error: Failed to save product (check permissions).");
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Error: Invalid number format for price or stock.");
        }
    }
    
    @FXML
    public void handleRemoveProduct() {
        Product selectedProduct = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            messageLabel.setText("Error: Please select a product to remove.");
            return;
        }

        // Execute the Remove Product sequence diagram logic
        String result = inventoryService.removeProduct(
            AuthenticationService.getActiveUser(), 
            selectedProduct.getProductID()
        );
        
        if (result.startsWith("Error")) {
            messageLabel.setText(result);
        } else {
            messageLabel.setText(result);
            loadProducts(); // Refresh table
            handleClearForm();
        }
    }
}