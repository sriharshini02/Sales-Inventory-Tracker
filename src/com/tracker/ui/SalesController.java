package com.tracker.ui;

import com.tracker.model.Product;
import com.tracker.service.AuthenticationService;
import com.tracker.service.InventoryService;
import com.tracker.service.SalesService;
import com.tracker.service.SalesService.SaleRequest;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesController {

    @FXML private TextField saleIDField;
    @FXML private TextField saleQuantityField;
    @FXML private TextField paymentMethodField;
    @FXML private Label cartMessageLabel;
    @FXML private Label totalLabel;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartIDCol;
    @FXML private TableColumn<CartItem, String> cartNameCol;
    @FXML private TableColumn<CartItem, Integer> cartQtyCol;
    @FXML private TableColumn<CartItem, Double> cartPriceCol;
    @FXML private TableColumn<CartItem, Double> cartTotalCol;

    private final InventoryService inventoryService = new InventoryService();
    private final SalesService salesService = new SalesService(inventoryService);
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupCartTable();
        cartTable.setItems(cartItems);
        updateTotal();
    }
    
    private void setupCartTable() {
        cartIDCol.setCellValueFactory(new PropertyValueFactory<>("productID"));
        cartNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        cartQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        cartTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
    }
    
    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        totalLabel.setText(String.format("$%.2f", total));
    }
    
    @FXML
    public void handleAddToCart() {
        String productId = saleIDField.getText().trim();
        int quantity;
        try {
            quantity = Integer.parseInt(saleQuantityField.getText());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            cartMessageLabel.setText("Error: Invalid quantity.");
            return;
        }

        Optional<Product> productOpt = inventoryService.getProductById(productId);
        
        if (productOpt.isEmpty()) {
            cartMessageLabel.setText("Error: Product ID not found.");
            return;
        }
        
        Product product = productOpt.get();
        
        if (product.getStockQuantity() < quantity) {
            cartMessageLabel.setText("Error: Insufficient stock. Available: " + product.getStockQuantity());
            return;
        }
        
        // Check if item is already in cart to consolidate
        Optional<CartItem> existingItem = cartItems.stream()
            .filter(item -> item.getProductID().equals(productId))
            .findFirst();
            
        if (existingItem.isPresent()) {
            // Simple update: assumes the request is for an ADDITIONAL quantity
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            cartTable.refresh();
        } else {
            // Add new item
            cartItems.add(new CartItem(productId, product.getName(), quantity, product.getSellingPrice()));
        }

        updateTotal();
        saleIDField.clear();
        saleQuantityField.clear();
        cartMessageLabel.setText("Item added to cart.");
    }
    
    @FXML
    public void handleRecordSale() {
        if (cartItems.isEmpty()) {
            cartMessageLabel.setText("Error: Cart is empty.");
            return;
        }
        
        String paymentMethod = paymentMethodField.getText().trim();
        if (paymentMethod.isEmpty()) {
            cartMessageLabel.setText("Error: Please specify Payment Method.");
            return;
        }
        
        // Convert CartItems to the Service-required SaleRequest objects
        List<SaleRequest> requests = new ArrayList<>();
        for (CartItem item : cartItems) {
            requests.add(new SaleRequest(item.getProductID(), item.getQuantity()));
        }

        // Call the service method to execute the transaction logic
        String result = salesService.recordSaleTransaction(
            AuthenticationService.getActiveUser(), 
            requests, 
            paymentMethod
        );

        // Process result
        if (result.startsWith("Error")) {
            cartMessageLabel.setText(result);
        } else {
            // Transaction successful
            cartMessageLabel.setText(result);
            cartItems.clear(); // Empty the cart
            paymentMethodField.clear();
            updateTotal();
        }
    }
    
    // --- Inner Class for TableView ---
    public static class CartItem {
        private final String productID;
        private final String name;
        private final Double price;
        private IntegerProperty quantity;
        private DoubleProperty total;

        public CartItem(String productID, String name, int quantity, double price) {
            this.productID = productID;
            this.name = name;
            this.price = price;
            this.quantity = new SimpleIntegerProperty(quantity);
            this.total = new SimpleDoubleProperty(quantity * price);
            
            // Listener to update total when quantity changes
            this.quantity.addListener((obs, oldVal, newVal) -> 
                this.total.set(newVal.intValue() * this.price));
        }

        // Getters for TableView PropertyValueFactory
        public String getProductID() { return productID; }
        public String getName() { return name; }
        public double getPrice() { return price; }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int quantity) { this.quantity.set(quantity); }
        public IntegerProperty quantityProperty() { return quantity; }

        public double getTotal() { return total.get(); }
        public DoubleProperty totalProperty() { return total; }
    }
}