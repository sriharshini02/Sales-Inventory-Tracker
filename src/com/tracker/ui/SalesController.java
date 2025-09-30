package com.tracker.ui;

import com.tracker.model.Product;
import com.tracker.service.AuthenticationService;
import com.tracker.service.InventoryService;
import com.tracker.service.SalesService;
import com.tracker.service.SalesService.SaleRequest;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.util.StringConverter;
import java.util.stream.Collectors;

public class SalesController {

    @FXML private ComboBox<Product> productSearchComboBox;
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
    private List<Product> cachedAllProducts;

    @FXML
    public void initialize() {
        if (productSearchComboBox == null) {
            System.err.println("productSearchComboBox is null! Check FXML loading.");
            return;
        }

        cachedAllProducts = inventoryService.getAllProducts();
        setupCartTable();
        setupProductSearch();
        cartTable.setItems(cartItems);
        updateTotal();
    }

    private void setupProductSearch() {
        productSearchComboBox.setEditable(true);

        productSearchComboBox.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                return product != null ? product.getName() + " (ID: " + product.getProductID() + ")" : "";
            }

            @Override
            public Product fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return cachedAllProducts.stream()
                        .filter(p -> (p.getName() + " (ID: " + p.getProductID() + ")").equalsIgnoreCase(string)
                                  || p.getName().equalsIgnoreCase(string)
                                  || p.getProductID().equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });

        // Use a single ObservableList to avoid replacing the list frequently
        ObservableList<Product> filteredProducts = FXCollections.observableArrayList(cachedAllProducts);
        productSearchComboBox.setItems(filteredProducts);

        // Autocomplete filter
        productSearchComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;

            String query = newText.toLowerCase();
            List<Product> matches = cachedAllProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(query)
                              || p.getProductID().toLowerCase().contains(query))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                filteredProducts.setAll(matches.isEmpty() ? cachedAllProducts : matches);
                productSearchComboBox.hide(); // Hide then show avoids flickering
                productSearchComboBox.show();
            });
        });

        // Selection listener
        productSearchComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                Platform.runLater(() -> productSearchComboBox.getEditor().setText(
                        productSearchComboBox.getConverter().toString(newSel)
                ));
            }
        });
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
        Product selectedProduct = productSearchComboBox.getSelectionModel().getSelectedItem();
        String typedText = productSearchComboBox.getEditor().getText().trim();

        if (selectedProduct == null) {
            selectedProduct = cachedAllProducts.stream()
                .filter(p -> p.getName().equalsIgnoreCase(typedText)
                          || p.getProductID().equalsIgnoreCase(typedText))
                .findFirst().orElse(null);
        }

        if (selectedProduct == null) {
            cartMessageLabel.setText("Error: Please select a product from the dropdown list.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(saleQuantityField.getText());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            cartMessageLabel.setText("Error: Invalid quantity.");
            return;
        }

        Product finalSelectedProduct = selectedProduct; // make a final copy

        int currentInCart = cartItems.stream()
                .filter(item -> item.getProductID().equals(finalSelectedProduct.getProductID()))
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (selectedProduct.getStockQuantity() < currentInCart + quantity) {
            cartMessageLabel.setText("Error: Insufficient stock. Available: " 
                                     + selectedProduct.getStockQuantity() 
                                     + ", In Cart: " + currentInCart);
            return;
        }
        Product finalSelectedProduct1 = selectedProduct; // make a final copy

        Optional<CartItem> existingItem = cartItems.stream()
            .filter(item -> item.getProductID().equals(finalSelectedProduct1.getProductID()))
            .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            cartTable.refresh();
        } else {
            cartItems.add(new CartItem(selectedProduct.getProductID(), selectedProduct.getName(), quantity, selectedProduct.getSellingPrice()));
        }

        updateTotal();
        productSearchComboBox.getSelectionModel().clearSelection();
        productSearchComboBox.getEditor().clear();
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

        List<SaleRequest> requests = new ArrayList<>();
        for (CartItem item : cartItems) {
            requests.add(new SaleRequest(item.getProductID(), item.getQuantity()));
        }

        String result = salesService.recordSaleTransaction(
            AuthenticationService.getActiveUser(),
            requests,
            paymentMethod
        );

        if (result.startsWith("Error")) {
            cartMessageLabel.setText(result);
        } else {
            cartMessageLabel.setText(result);
            cartItems.clear();
            paymentMethodField.clear();
            updateTotal();
        }
    }


    public static class CartItem {
        private final String productID;
        private final String name;
        private final Double price;
        private javafx.beans.property.IntegerProperty quantity;
        private javafx.beans.property.DoubleProperty total;

        public CartItem(String productID, String name, int quantity, double price) {
            this.productID = productID;
            this.name = name;
            this.price = price;
            this.quantity = new javafx.beans.property.SimpleIntegerProperty(quantity);
            this.total = new javafx.beans.property.SimpleDoubleProperty(quantity * price);

            this.quantity.addListener((obs, oldVal, newVal) ->
                    this.total.set(newVal.intValue() * this.price));
        }

        public String getProductID() { return productID; }
        public String getName() { return name; }
        public double getPrice() { return price; }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int quantity) { this.quantity.set(quantity); }
        public javafx.beans.property.IntegerProperty quantityProperty() { return quantity; }

        public double getTotal() { return total.get(); }
        public javafx.beans.property.DoubleProperty totalProperty() { return total; }
    }
}
