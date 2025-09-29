package com.tracker.model;

import java.io.Serializable;

/**
 * Represents a single line item in a sales transaction.
 * Corresponds to the 'Sale' class in the diagram.
 */
public class Sale implements Serializable {
    private static final long serialVersionUID = 1L;

    private String saleID;
    private String productID;
    private int quantity;
    private double sellingPrice; // Final price per unit at time of sale
    private double costPrice;    // Cost price at time of sale (for reporting)

    public Sale(String saleID, String productID, int quantity, double sellingPrice, double costPrice) {
        this.saleID = saleID;
        this.productID = productID;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
    }

    // Getters
    public String getSaleID() { return saleID; }
    public String getProductID() { return productID; }
    public int getQuantity() { return quantity; }
    public double getSellingPrice() { return sellingPrice; }
    public double getCostPrice() { return costPrice; }
    
    // Calculated field
    public double getLineTotal() {
        return quantity * sellingPrice;
    }
    public double getLineCost() {
        return quantity * costPrice;
    }
}