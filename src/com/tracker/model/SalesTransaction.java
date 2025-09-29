package com.tracker.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public SalesTransaction(String transactionID, String paymentMethod) {
        this.transactionID = transactionID;
        this.dateTime = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.sales = new ArrayList<>();
        this.calculatedTotal = 0.0;
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
    
    // Utility for reporting
    public double getTotalCostOfGoodsSold() {
        return this.sales.stream()
                         .mapToDouble(Sale::getLineCost)
                         .sum();
    }
}