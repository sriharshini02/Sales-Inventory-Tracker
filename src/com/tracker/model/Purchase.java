package com.tracker.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a single purchase record of goods from a supplier.
 */
public class Purchase implements Serializable {
    private static final long serialVersionUID = 1L;

    private String purchaseID;
    private String productID;
    private int quantity;
    private double costPrice;
    private LocalDate purchaseDate;
    private String supplierName; // Storing name directly for simplified data entry

    public Purchase(String purchaseID, String productID, int quantity, double costPrice, LocalDate purchaseDate, String supplierName) {
        this.purchaseID = purchaseID;
        this.productID = productID;
        this.quantity = quantity;
        this.costPrice = costPrice;
        this.purchaseDate = purchaseDate;
        this.supplierName = supplierName;
    }

    // Getters
    public String getPurchaseID() { return purchaseID; }
    public String getProductID() { return productID; }
    public int getQuantity() { return quantity; }
    public double getCostPrice() { return costPrice; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public String getSupplierName() { return supplierName; }
}