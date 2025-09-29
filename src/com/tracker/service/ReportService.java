package com.tracker.service;

import com.tracker.dao.SalesDAO;
import com.tracker.model.SalesTransaction;
import com.tracker.model.Sale;
import com.tracker.model.Report;
import com.tracker.model.User;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executes the business logic for the Generate Report use case (FR-6, FR-7).
 */
public class ReportService {

    private final SalesDAO salesDAO;

    public ReportService(SalesDAO salesDAO) {
        this.salesDAO = salesDAO;
    }

    // Utility to enforce ShopKeeper access
    private boolean checkShopKeeperAccess(User user) {
        if (user == null || !user.getRole().equals("SHOPKEEPER")) {
            System.err.println("Access Denied: Only ShopKeeper can generate reports.");
            return false;
        }
        return true;
    }

    // --- Use Case: Generate Profit/Loss Report (FR-6) ---
    public Report generateProfitLossReport(User user, LocalDate startDate, LocalDate endDate) {
        if (!checkShopKeeperAccess(user)) {
            return new Report("Access Denied", startDate, endDate);
        }
        
        // 1. fetchData(type)
        List<SalesTransaction> transactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        
        // Calculation logic
        double totalRevenue = transactions.stream().mapToDouble(SalesTransaction::getCalculatedTotal).sum();
        double totalCost = transactions.stream().mapToDouble(SalesTransaction::getTotalCostOfGoodsSold).sum();
        double profitLoss = totalRevenue - totalCost;
        
        // 2. formatData(type)
        String content = String.format(
            "Time Period: %s to %s\n" +
            "-----------------------------------------\n" +
            "Total Revenue (Sales): $%.2f\n" +
            "Total Cost of Goods Sold (COGS): $%.2f\n" +
            "NET PROFIT/LOSS: $%.2f\n",
            startDate, endDate, totalRevenue, totalCost, profitLoss
        );

        Report report = new Report("Profit/Loss", startDate, endDate);
        report.setReportContent(content);
        return report;
    }

    // --- Use Case: Generate Best Selling Report (FR-7) ---
    public Report generateBestSellingReport(User user, LocalDate startDate, LocalDate endDate, int topN) {
        if (!checkShopKeeperAccess(user)) {
            return new Report("Access Denied", startDate, endDate);
        }
        
        List<SalesTransaction> transactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        
        // Group all sales items by Product ID and sum their quantities
        Map<String, Integer> salesByProduct = transactions.stream()
            .flatMap(t -> t.getSales().stream()) // Flatten all Sale items
            .collect(Collectors.groupingBy(
                Sale::getProductID,
                Collectors.summingInt(Sale::getQuantity)
            ));
        
        // Sort, limit to top N, and format
        String content = salesByProduct.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(topN)
            .map(entry -> String.format("Product ID: %s, Quantity Sold: %d\n", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining());

        String header = String.format("Time Period: %s to %s\n-----------------------------------------\n", 
                                       startDate, endDate);

        Report report = new Report("Best Selling", startDate, endDate);
        report.setReportContent(header + content);
        return report;
    }
}