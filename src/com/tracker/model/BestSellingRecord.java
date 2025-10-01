package com.tracker.model;

import java.time.LocalDate;

/**
 * POJO for holding structured data for the Best Selling report.
 * (Replaces Java 16 'record' for Java 8 compatibility)
 */
public class BestSellingRecord {
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final String productId;
    private final String productName;
    private final int quantitySold;

    public BestSellingRecord(LocalDate periodStart, LocalDate periodEnd, String productId, String productName, int quantitySold) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.productId = productId;
        this.productName = productName;
        this.quantitySold = quantitySold;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }
}
