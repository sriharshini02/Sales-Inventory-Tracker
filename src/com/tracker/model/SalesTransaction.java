package com.tracker.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
/**
 * Represents a complete sales transaction.
 * Corresponds to the 'SalesTransaction' class in the diagram.
 */
public class SalesTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionID;
    private LocalDateTime dateTime;
    private String paymentMethod;
    private List<Sale> sales; // List<Sale> implements the 1..* relationship
    private double calculatedTotal;
    private final LocalDateTime timestamp; // Store as LocalDateTime
    private final User user; // Store the user object
    
    public SalesTransaction(String transactionID, User user, String paymentMethod) {
        this.transactionID = transactionID;
        this.user = user; // Store the user
        this.paymentMethod = paymentMethod;
        this.timestamp = LocalDateTime.now(); // Set the timestamp upon creation
        this.sales = new ArrayList<>();
    }

    // Corresponds to addSale(Sale sale) in the diagram
    public void addSale(Sale sale) {
        this.sales.add(sale);
        calculateTotal();
    }

    // Corresponds to calculateTotal() in the diagram
    public void calculateTotal() {
        this.calculatedTotal = this.sales.stream()
                                         .mapToDouble(Sale::getLineTotal)
                                         .sum();
    }
    
    // Getters
    public String getTransactionID() { return transactionID; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getPaymentMethod() { return paymentMethod; }
    public List<Sale> getSales() { return sales; }
    public double getCalculatedTotal() { return calculatedTotal; }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public String getFormattedTimestamp() {
        // FIX: Check if timestamp is null before calling format()
        if (this.timestamp == null) {
            return "N/A (Legacy Data)"; 
        }
        
        // NOTE: Ensure DateTimeFormatter is imported or fully qualified if needed
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
        return this.timestamp.format(formatter);
    }
    public User getUser() {
        return user;
    }
    
    // Utility for reporting
    public double getTotalCostOfGoodsSold() {
        return this.sales.stream()
                         .mapToDouble(Sale::getLineCost)
                         .sum();
    }
    
}