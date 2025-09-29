package com.tracker.model;

import java.io.Serializable;

/**
 * Represents a single product managed in the inventory. 
 * Corresponds directly to the 'Product' class in the UML Class Diagram.
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L; 
    
    // Attributes from Class Diagram
    private String productID; // Unique ID (Primary Key)
    private String name;
    private String category;
    private double costPrice;
    private double sellingPrice;
    private int stockQuantity; // Corresponds to the 'stock' attribute

    // Constructor
    public Product(String productID, String name, String category, double costPrice, double sellingPrice, int initialStock) {
        this.productID = productID;
        this.name = name;
        this.category = category;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = initialStock;
    }

    // Getters
    public String getProductID() { return productID; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getCostPrice() { return costPrice; }
    public double getSellingPrice() { return sellingPrice; }
    public int getStockQuantity() { return stockQuantity; } // Implements getCurrentStock()

    // Setters (Only for mutable attributes/properties that can be updated)
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; } // Updated upon new purchase

    // Methods from Class Diagram (Crucial for inventory logic)
    
    /**
     * Updates the stock quantity. 
     * Positive value increases stock (Purchase).
     * Negative value decreases stock (Sale).
     */
    public void updateStock(int quantityChange) {
        this.stockQuantity += quantityChange;
    }
}