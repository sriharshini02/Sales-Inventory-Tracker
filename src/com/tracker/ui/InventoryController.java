package com.tracker.ui;

import com.tracker.model.Product;
import com.tracker.service.InventoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class InventoryController {

    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, String> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, Double> costCol;
    @FXML private TableColumn<Product, Double> sellCol;
    @FXML private TableColumn<Product, Integer> stockCol;

    private final InventoryService inventoryService = new InventoryService();

    @FXML
    public void initialize() {
        // Set up the columns to map to the Product model attributes
        idCol.setCellValueFactory(new PropertyValueFactory<>("productID"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        sellCol.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Initial load of data
        loadInventoryData();
    }

    private void loadInventoryData() {
        // Executes the 'View Current Stock' sequence: UI calls Service, Service calls DAO
        inventoryTable.setItems(FXCollections.observableArrayList(inventoryService.viewCurrentStock()));
    }
}