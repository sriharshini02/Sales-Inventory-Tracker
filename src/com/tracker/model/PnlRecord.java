package com.tracker.model;

import java.time.LocalDate;

/**
 * POJO for holding structured data for the Profit and Loss report.
 * (Replaces Java 16 'record' for Java 8 compatibility)
 */
public class PnlRecord {
    private final LocalDate periodStart; 
    private final LocalDate periodEnd; 
    private final double revenue; 
    private final double profitLoss;

    public PnlRecord(LocalDate periodStart, LocalDate periodEnd, double revenue, double profitLoss) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.revenue = revenue;
        this.profitLoss = profitLoss;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getProfitLoss() {
        return profitLoss;
    }
}

