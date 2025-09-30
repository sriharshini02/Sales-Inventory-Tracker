package com.tracker.model;

import java.io.Serializable;

/**
 * Represents a single line item in a sales transaction.
 */
public class Sale implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- CORE SALE DATA (MATCHING FXML COLUMN BINDINGS) ---
    private String saleID;
    private String productID;
    private int quantitySold;       // Binds to 'colQuantity' (via getQuantitySold)
    private double unitSalePrice;   // Binds to 'colSalePrice' (via getUnitSalePrice)
    private double unitCostPrice;   // Cost price at time of sale

    // --- CONTEXT DATA (SET BY CONTROLLER/SERVICE) ---
    private String transactionID;   // Binds to 'colTransactionID'
    private String productName;     // Binds to 'colProductName'
    private String timestamp;       // Binds to 'colTimestamp'
    private String recordedBy;      // Binds to 'colUser'


    // Constructor: Takes the basic sale item details
    public Sale(String saleID, String productID, int quantity, double sellingPrice, double costPrice) {
        this.saleID = saleID;
        this.productID = productID;
        
        // Map constructor arguments to the standardized field names
        this.quantitySold = quantity;
        this.unitSalePrice = sellingPrice; 
        this.unitCostPrice = costPrice;
        
        // Initialize context fields
        this.transactionID = "";
        this.productName = "";
        this.timestamp = "";
        this.recordedBy = "";
    }

    // --- REQUIRED PUBLIC GETTERS (The FXML PropertyValueFactory checks for these) ---

    // Fix 1: Getter for 'quantitySold' (colQuantity)
    public int getQuantitySold() { 
        return quantitySold; 
    }
    
    // Fix 2: Getter for 'unitSalePrice' (colSalePrice)
    public double getUnitSalePrice() { 
        return unitSalePrice; 
    }

    // Fix 3: Calculated property for 'totalRevenue'
    public double getTotalRevenue() {
        return this.quantitySold * this.unitSalePrice;
    }

    // Other Getters
    public String getSaleID() { return saleID; }
    public String getProductID() { return productID; }
    public double getUnitCostPrice() { return unitCostPrice; }
    
    public String getTransactionID() { return transactionID; }
    public String getTimestamp() { return timestamp; }
    public String getProductName() { 
        return productName;
    }
    public String getRecordedBy() { return recordedBy; }
    public int getQuantity() { return quantitySold; }
    
    // --- REQUIRED SETTERS (Used by SalesHistoryController to add context) ---
    
    public void setTransactionID(String transactionID) { this.transactionID = transactionID; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public void setProductName(String productName) { this.productName = productName; }
    
    
    // --- Compatibility/Convenience Getters (Optional, but useful) ---
    
    public double getLineTotal() {
        return getTotalRevenue();
    }
    public double getLineCost() {
        return quantitySold * unitCostPrice;
    }
}