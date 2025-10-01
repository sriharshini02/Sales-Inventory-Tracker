package com.tracker.service;

import com.tracker.dao.ProductDAO;
import com.tracker.dao.SalesDAO;
import com.tracker.model.SalesTransaction;
import com.tracker.model.Sale;
import com.tracker.model.Product;
import com.tracker.model.Report;
import com.tracker.model.User;
// Import the Java 8 POJO classes
import com.tracker.model.PnlRecord; 
import com.tracker.model.BestSellingRecord; 

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Executes the business logic for the Generate Report use case (FR-6, FR-7).
 */
public class ReportService {

    private final SalesDAO salesDAO;
    private final ProductDAO productDAO; 
    
    // CRITICAL: Set a reasonable limit for the number of periods (spans) to report.
    private static final int MAX_REPORT_SPANS = 60;

    public ReportService(SalesDAO salesDAO) {
        this.salesDAO = salesDAO;
        this.productDAO = new ProductDAO(); 
    }

    // Utility to enforce ShopKeeper access
    private boolean checkShopKeeperAccess(User user) {
        if (user == null || !user.getRole().equals("SHOPKEEPER")) {
            System.err.println("Access Denied: Only ShopKeeper can generate reports.");
            return false;
        }
        return true;
    }
    /**
     * Helper to calculate corrected spanDays, ensuring it is at least 1.
     */
    
    private double[] calculatePnlMetrics(List<SalesTransaction> transactions) {
        double totalRevenue = transactions.stream().mapToDouble(SalesTransaction::getCalculatedTotal).sum();
        double totalCost = transactions.stream().mapToDouble(SalesTransaction::getTotalCostOfGoodsSold).sum();
        double profitLoss = totalRevenue - totalCost;
        return new double[]{totalRevenue, totalCost, profitLoss};
    }

    /**
     * Helper to calculate corrected spanDays, ensuring it is at least 1.
     */
    private int getSafeSpanDays(int spanDays) {
        // CRITICAL FIX: Ensure spanDays is at least 1 to prevent division by zero or infinite loop
        return Math.max(1, spanDays);
    }
    
    public List<PnlRecord> getPnlRecords(User user, LocalDate startDate, LocalDate endDate, int spanDays) {
        if (!checkShopKeeperAccess(user)) {
            return new ArrayList<>(); 
        }
        
        int safeSpanDays = getSafeSpanDays(spanDays);
        List<SalesTransaction> allTransactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        List<PnlRecord> records = new ArrayList<>();
        
        LocalDate currentStart = startDate;
        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plusDays(safeSpanDays - 1);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate; 
            }

            final LocalDate finalEnd = currentEnd;
            final LocalDate start = currentStart;
            List<SalesTransaction> spanTransactions = allTransactions.stream()
                .filter(t -> t.getDateTime() != null)
                .filter(t -> t.getDateTime().toLocalDate().isAfter(start.minusDays(1)) && 
                           t.getDateTime().toLocalDate().isBefore(finalEnd.plusDays(1)))
                .collect(Collectors.toList());

            double[] metrics = calculatePnlMetrics(spanTransactions);
            
            // Add structured record
            records.add(new PnlRecord(currentStart, finalEnd, metrics[0], metrics[2]));

            currentStart = currentEnd.plusDays(1); 
        }
        
        return records;
    }

    /**
     * Generates a list of BestSellingRecord objects for CSV export, including all detailed spans
     * and a final aggregated span for the overall summary.
     */
    public List<BestSellingRecord> getBestSellingRecords(User user, LocalDate startDate, LocalDate endDate, int topN, int spanDays) {
        if (!checkShopKeeperAccess(user)) {
            return new ArrayList<>();
        }
        
        int safeSpanDays = getSafeSpanDays(spanDays);
        List<SalesTransaction> allTransactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        List<BestSellingRecord> records = new ArrayList<>();
        
        // --- 1. PERIOD BREAKDOWN RECORDS ---
        LocalDate currentStart = startDate;
        
        while (!currentStart.isAfter(endDate)) {
            LocalDate currentEnd = currentStart.plusDays(safeSpanDays - 1);
            if (currentEnd.isAfter(endDate)) {
                currentEnd = endDate; 
            }
            
            final LocalDate finalEnd = currentEnd;
            final LocalDate start = currentStart;
            List<SalesTransaction> spanTransactions = allTransactions.stream()
                .filter(t -> t.getDateTime() != null)
                .filter(t -> t.getDateTime().toLocalDate().isAfter(start.minusDays(1)) && 
                           t.getDateTime().toLocalDate().isBefore(finalEnd.plusDays(1)))
                .collect(Collectors.toList());

            Map<String, Integer> spanSalesByProduct = spanTransactions.stream()
                .flatMap(t -> t.getSales().stream())
                .collect(Collectors.groupingBy(
                    Sale::getProductID,
                    Collectors.summingInt(Sale::getQuantity)
                ));

            List<Map.Entry<String, Integer>> topNSales = spanSalesByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .collect(Collectors.toList());
            
            for (Map.Entry<String, Integer> entry : topNSales) {
                Optional<Product> productOpt = productDAO.getProductById(entry.getKey());
                String productName = productOpt.map(Product::getName).orElse("UNKNOWN PRODUCT");
                
                // Add structured record for the period
                records.add(new BestSellingRecord(
                    currentStart, 
                    finalEnd, 
                    entry.getKey(), 
                    productName, 
                    entry.getValue()
                ));
            }

            currentStart = currentEnd.plusDays(1);
        }
        
        // --- 2. OVERALL SUMMARY RECORD (Aggregated) ---
        
        Map<String, Integer> totalSalesByProduct = allTransactions.stream()
            .flatMap(t -> t.getSales().stream())
            .collect(Collectors.groupingBy(
                Sale::getProductID,
                Collectors.summingInt(Sale::getQuantity)
            ));
            
        List<Map.Entry<String, Integer>> overallTopNSales = totalSalesByProduct.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(topN)
            .collect(Collectors.toList());

        LocalDate overallStart = startDate;
        LocalDate overallEnd = endDate;

        for (Map.Entry<String, Integer> entry : overallTopNSales) {
            Optional<Product> productOpt = productDAO.getProductById(entry.getKey());
            String productName = productOpt.map(Product::getName).orElse("UNKNOWN PRODUCT") + " (Overall)";
            
            // Add structured record for the overall summary
            records.add(new BestSellingRecord(
                overallStart, 
                overallEnd,   
                entry.getKey(), 
                productName, 
                entry.getValue()
            ));
        }

        return records;
    }

    // =========================================================================
    // TEXT REPORT GENERATORS (FOR UI DISPLAY)
    // =========================================================================
    
    // --- Use Case: Generate Profit/Loss Report (FR-6) ---
    public Report generateProfitLossReport(User user, LocalDate startDate, LocalDate endDate, int spanDays) {
        if (!checkShopKeeperAccess(user)) {
            return new Report("Access Denied", startDate, endDate);
        }

        int safeSpanDays = getSafeSpanDays(spanDays);
        List<SalesTransaction> allTransactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        
        // CRITICAL CHECK FOR MEMORY
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long numSpans = (long) Math.ceil((double) totalDays / safeSpanDays);
        boolean skipBreakdown = numSpans > MAX_REPORT_SPANS;
        
        StringBuilder reportBuilder = new StringBuilder(skipBreakdown ? 512 : 16384);
        
        final String LINE = "========================================================================\n";
        final String HEADER_FORMAT = "%-30s %15s %15s\n";
        final String DETAIL_FORMAT = "%-30s %15.2f %15.2f\n";
        
        reportBuilder.append("PROFIT AND LOSS REPORT WITH SPAN BREAKDOWN\n");
        reportBuilder.append(String.format("Time Period: %s to %s | Span: %d Days\n\n", 
                                            startDate.toString(), endDate.toString(), safeSpanDays));

        // --- SECTION A: Breakdown Report ---
        if (skipBreakdown) {
             reportBuilder.append(LINE);
             reportBuilder.append(String.format("NOTICE: The date range (%d days) with the chosen span (%d days) results in %d periods.\n", totalDays, safeSpanDays, numSpans));
             reportBuilder.append(String.format("To prevent OutOfMemoryError, the detailed breakdown is skipped.\n"));
             reportBuilder.append("Please choose a larger span or shorter date range.\n");
             reportBuilder.append(LINE);
        } else {
            reportBuilder.append(LINE);
            reportBuilder.append("A. PERIOD BREAKDOWN\n");
            reportBuilder.append(LINE);
            
            reportBuilder.append(String.format(HEADER_FORMAT, "Period", "Revenue ($)", "Profit/(Loss) ($)"));
            reportBuilder.append(LINE);
            
            LocalDate currentStart = startDate;
            while (!currentStart.isAfter(endDate)) {
                LocalDate currentEnd = currentStart.plusDays(safeSpanDays - 1);
                if (currentEnd.isAfter(endDate)) {
                    currentEnd = endDate; 
                }

                final LocalDate finalEnd = currentEnd;
                final LocalDate start = currentStart;
                List<SalesTransaction> spanTransactions = allTransactions.stream()
                    .filter(t -> t.getDateTime() != null)
                    .filter(t -> t.getDateTime().toLocalDate().isAfter(start.minusDays(1)) && 
                               t.getDateTime().toLocalDate().isBefore(finalEnd.plusDays(1)))
                    .collect(Collectors.toList());

                double[] metrics = calculatePnlMetrics(spanTransactions);
                double revenue = metrics[0];
                double profitLoss = metrics[2];

                String periodLabel = currentStart.isEqual(currentEnd) ? 
                                        currentStart.toString() : 
                                        currentStart.toString() + " to " + finalEnd.toString();
                
                reportBuilder.append(String.format(DETAIL_FORMAT, 
                    periodLabel, 
                    revenue, 
                    profitLoss
                ));

                currentStart = currentEnd.plusDays(1); 
            }
        } 
        
        // --- SECTION B: Total Summary ---
        reportBuilder.append("\n\n");
        reportBuilder.append(LINE);
        reportBuilder.append("B. OVERALL SUMMARY\n");
        reportBuilder.append(LINE);
        
        double[] totalMetrics = calculatePnlMetrics(allTransactions);
        double totalRevenue = totalMetrics[0];
        double totalCost = totalMetrics[1];
        double totalProfitLoss = totalMetrics[2];

        reportBuilder.append(String.format("%-40s %15.2f\n", "TOTAL REVENUE (SALES):", totalRevenue));
        reportBuilder.append(String.format("%-40s %15.2f\n", "TOTAL COST OF GOODS SOLD (COGS):", totalCost));
        
        reportBuilder.append(LINE);
        String profitLabel = totalProfitLoss >= 0 ? "NET PROFIT" : "NET LOSS";
        reportBuilder.append(String.format("%-40s %15.2f\n", profitLabel + ":", totalProfitLoss));
        reportBuilder.append(LINE);

        Report report = new Report("Profit/Loss", startDate, endDate);
        report.setReportContent(reportBuilder.toString());
        return report;
    }

    // --- Use Case: Generate Best Selling Report (FR-7) ---
    public Report generateBestSellingReport(User user, LocalDate startDate, LocalDate endDate, int topN, int spanDays) {
        if (!checkShopKeeperAccess(user)) {
            return new Report("Access Denied", startDate, endDate);
        }
        
        int safeSpanDays = getSafeSpanDays(spanDays);
        List<SalesTransaction> allTransactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        
        // CRITICAL CHECK FOR MEMORY
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long numSpans = (long) Math.ceil((double) totalDays / safeSpanDays);
        boolean skipBreakdown = numSpans > MAX_REPORT_SPANS;

        StringBuilder reportBuilder = new StringBuilder(skipBreakdown ? 512 : 16384);
        
        final String LINE = "========================================================================\n";
        final String HEADER_FORMAT = "%-15s %-40s %10s\n";

        reportBuilder.append("BEST SELLING REPORT WITH SPAN BREAKDOWN\n");
        reportBuilder.append(String.format("Time Period: %s to %s | Span: %d Days | Top %d\n\n", 
                                            startDate.toString(), endDate.toString(), safeSpanDays, topN));
        
        // --- SECTION A: Breakdown Report ---
        if (skipBreakdown) {
             reportBuilder.append(LINE);
             reportBuilder.append(String.format("NOTICE: The date range (%d days) with the chosen span (%d days) results in %d periods.\n", totalDays, safeSpanDays, numSpans));
             reportBuilder.append(String.format("To prevent OutOfMemoryError, the detailed breakdown is skipped.\n"));
             reportBuilder.append("Please choose a larger span or shorter date range.\n");
             reportBuilder.append(LINE);
        } else {
            LocalDate currentStart = startDate;
            
            while (!currentStart.isAfter(endDate)) {
                LocalDate currentEnd = currentStart.plusDays(safeSpanDays - 1);
                if (currentEnd.isAfter(endDate)) {
                    currentEnd = endDate; 
                }
                
                String periodLabel = currentStart.isEqual(currentEnd) ? 
                                        currentStart.toString() : 
                                        currentStart.toString() + " to " + currentEnd.toString();
                
                reportBuilder.append(LINE);
                reportBuilder.append(String.format("PERIOD: %s\n", periodLabel));
                reportBuilder.append(LINE);

                final LocalDate finalEnd = currentEnd;
                final LocalDate start = currentStart;
                List<SalesTransaction> spanTransactions = allTransactions.stream()
                    .filter(t -> t.getDateTime() != null) 
                    .filter(t -> t.getDateTime().toLocalDate().isAfter(start.minusDays(1)) && 
                               t.getDateTime().toLocalDate().isBefore(finalEnd.plusDays(1)))
                    .collect(Collectors.toList());

                Map<String, Integer> spanSalesByProduct = spanTransactions.stream()
                    .flatMap(t -> t.getSales().stream())
                    .collect(Collectors.groupingBy(
                        Sale::getProductID,
                        Collectors.summingInt(Sale::getQuantity)
                    ));

                List<Map.Entry<String, Integer>> topNSales = spanSalesByProduct.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(topN)
                    .collect(Collectors.toList());
                
                if (topNSales.isEmpty()) {
                    reportBuilder.append("No sales recorded in this period.\n\n");
                } else {
                    reportBuilder.append(String.format(HEADER_FORMAT, "ID", "Name", "Qty Sold"));
                    reportBuilder.append("--------------------------------------------------------------------\n");
                    
                    for (Map.Entry<String, Integer> entry : topNSales) {
                        Optional<Product> productOpt = productDAO.getProductById(entry.getKey());
                        String productName = productOpt.map(Product::getName).orElse("UNKNOWN PRODUCT");
                        
                        reportBuilder.append(String.format(HEADER_FORMAT, 
                                                            entry.getKey(), 
                                                            productName, 
                                                            entry.getValue()));
                    }
                    reportBuilder.append("\n");
                }

                currentStart = currentEnd.plusDays(1);
            }
        } 
        
        // --- SECTION B: Overall Summary ---
        reportBuilder.append("\n\n");
        reportBuilder.append(LINE);
        reportBuilder.append("B. OVERALL TOP " + topN + " PRODUCTS (FULL PERIOD)\n");
        reportBuilder.append(LINE);

        Map<String, Integer> totalSalesByProduct = allTransactions.stream()
            .flatMap(t -> t.getSales().stream())
            .collect(Collectors.groupingBy(
                Sale::getProductID,
                Collectors.summingInt(Sale::getQuantity)
            ));
            
        List<Map.Entry<String, Integer>> overallTopNSales = totalSalesByProduct.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(topN)
            .collect(Collectors.toList());

        if (overallTopNSales.isEmpty()) {
            reportBuilder.append("No sales recorded in the entire period.\n");
        } else {
            reportBuilder.append(String.format(HEADER_FORMAT, "ID", "Name", "Total Qty Sold"));
            reportBuilder.append("--------------------------------------------------------------------\n");
            
            for (Map.Entry<String, Integer> entry : overallTopNSales) {
                Optional<Product> productOpt = productDAO.getProductById(entry.getKey());
                String productName = productOpt.map(Product::getName).orElse("UNKNOWN PRODUCT");
                
                reportBuilder.append(String.format(HEADER_FORMAT, 
                                                    entry.getKey(), 
                                                    productName, 
                                                    entry.getValue()));
            }
        }
        reportBuilder.append(LINE);

        Report report = new Report("Best Selling", startDate, endDate);
        report.setReportContent(reportBuilder.toString());
        return report;
    }
}