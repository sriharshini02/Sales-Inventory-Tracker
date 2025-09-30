package com.tracker.ui;

import com.tracker.model.Sale; 
import com.tracker.model.SalesTransaction;
import com.tracker.service.InventoryService; // Import the dependency
import com.tracker.service.SalesService; 
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.List;
import com.tracker.model.User;
public class SalesHistoryController implements Initializable {

    // FXML fields remain the same (they are set up to bind to the Sale model)
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, String> colTransactionID;
    @FXML private TableColumn<Sale, String> colTimestamp;
    @FXML private TableColumn<Sale, String> colProductID;
    @FXML private TableColumn<Sale, String> colProductName; // NOTE: This will require a getter in Sale model
    @FXML private TableColumn<Sale, Integer> colQuantity;
    @FXML private TableColumn<Sale, Double> colSalePrice;
    @FXML private TableColumn<Sale, Double> colTotalRevenue;
    @FXML private TableColumn<Sale, String> colUser; // NOTE: This will require a getter in Sale model

    // FIX 1: Instantiate InventoryService first, then use it for SalesService
    private final InventoryService inventoryService = new InventoryService();
    private final SalesService salesService = new SalesService(inventoryService);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Map columns to the 'Sale' model properties (case-sensitive)
        colTransactionID.setCellValueFactory(new PropertyValueFactory<>("transactionID"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colProductID.setCellValueFactory(new PropertyValueFactory<>("productID"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName")); 
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        colSalePrice.setCellValueFactory(new PropertyValueFactory<>("unitSalePrice"));
        colTotalRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("recordedBy")); // Ensure these getters exist in Sale model

        loadSalesData();
    }

    private void loadSalesData() {
        salesTable.getItems().clear();
        
        List<SalesTransaction> transactions = salesService.viewSalesHistory();
        
        List<Sale> allSalesRecords = transactions.stream()
            // Map each SalesTransaction to its internal list of Sale objects
            .flatMap(transaction -> {
                // FIX: Safely retrieve the username
                User recordedUser = transaction.getUser();
                String recordedByName = (recordedUser != null) 
                    ? recordedUser.getUsername() 
                    : "Unknown User (Legacy)";

                transaction.getSales().forEach(sale -> {
                    sale.setTimestamp(transaction.getFormattedTimestamp()); 
                    
                    // FIX: Use the safely retrieved name
                    sale.setRecordedBy(recordedByName); 
                    
                    sale.setTransactionID(transaction.getTransactionID()); 
                });
                return transaction.getSales().stream();
            })
            .collect(Collectors.toList());
            
        salesTable.setItems(FXCollections.observableArrayList(allSalesRecords));
    }
}