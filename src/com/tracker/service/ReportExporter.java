package com.tracker.service;

import com.tracker.model.BestSellingRecord;
import com.tracker.model.PnlRecord;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class to convert structured report data into CSV format using standard Java operations.
 */
public class ReportExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Converts a list of P&L records into a CSV formatted String.
     * @param records The list of PnlRecord objects.
     * @return The CSV content as a String.
     */
    public String exportPnlToCsv(List<PnlRecord> records) {
        StringBuilder csvContent = new StringBuilder();
        
        // 1. Write Header
        csvContent.append("Period_Start_Date,Period_End_Date,Revenue,Profit_Loss\n");

        // 2. Write Records
        for (PnlRecord record : records) {
            csvContent.append(record.getPeriodStart().format(DATE_FORMATTER)).append(",");
            csvContent.append(record.getPeriodEnd().format(DATE_FORMATTER)).append(",");
            
            // Append formatted currency values (%.2f)
            csvContent.append(String.format("%.2f", record.getRevenue())).append(",");
            csvContent.append(String.format("%.2f", record.getProfitLoss())).append("\n");
        }
        return csvContent.toString();
    }

    /**
     * Converts a list of Best Selling records into a CSV formatted String.
     * @param records The list of BestSellingRecord objects.
     * @return The CSV content as a String.
     */
    public String exportBestSellingToCsv(List<BestSellingRecord> records) {
        StringBuilder csvContent = new StringBuilder();
        
        // 1. Write Header
        csvContent.append("Period_Start_Date,Period_End_Date,Product_ID,Product_Name,Quantity_Sold\n");

        // 2. Write Records
        for (BestSellingRecord record : records) {
            csvContent.append(record.getPeriodStart().format(DATE_FORMATTER)).append(",");
            csvContent.append(record.getPeriodEnd().format(DATE_FORMATTER)).append(",");
            csvContent.append(record.getProductId()).append(",");
            
            // Minimal CSV sanitation: if the product name contains a comma, enclose it in double quotes.
            String productName = record.getProductName();
            if (productName != null && productName.contains(",")) {
                csvContent.append("\"").append(productName).append("\"").append(",");
            } else {
                csvContent.append(productName).append(",");
            }
            
            csvContent.append(record.getQuantitySold()).append("\n");
        }
        return csvContent.toString();
    }
}