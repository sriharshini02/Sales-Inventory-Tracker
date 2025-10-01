package com.tracker.service;

import com.tracker.dao.ProductDAO;
import com.tracker.dao.SalesDAO;
import com.tracker.model.SalesTransaction;
import com.tracker.model.Sale;
import com.tracker.model.Product;
import com.tracker.model.Report;
import com.tracker.model.User;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    // If a report has too many small periods, the generated string will exceed heap space.
    private static final int MAX_REPORT_SPANS = 60; // Max 60 sections (e.g., 60 days, 60 weeks)

    public ReportService(SalesDAO salesDAO) {
        this.salesDAO = salesDAO;
        this.productDAO = new ProductDAO(); 
    }

    // ... (rest of checkShopKeeperAccess and calculatePnlMetrics remain the same) ...
    private boolean checkShopKeeperAccess(User user) {
        if (user == null || !user.getRole().equals("SHOPKEEPER")) {
            System.err.println("Access Denied: Only ShopKeeper can generate reports.");
            return false;
        }
        return true;
    }

    private double[] calculatePnlMetrics(List<SalesTransaction> transactions) {
        double totalRevenue = transactions.stream().mapToDouble(SalesTransaction::getCalculatedTotal).sum();
        double totalCost = transactions.stream().mapToDouble(SalesTransaction::getTotalCostOfGoodsSold).sum();
        double profitLoss = totalRevenue - totalCost;
        return new double[]{totalRevenue, totalCost, profitLoss};
    }

    // --- Use Case: Generate Profit/Loss Report (FR-6) ---
    public Report generateProfitLossReport(User user, LocalDate startDate, LocalDate endDate, int spanDays) {
        if (!checkShopKeeperAccess(user)) {
            return new Report("Access Denied", startDate, endDate);
        }

        List<SalesTransaction> allTransactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        
        // CRITICAL CHECK FOR MEMORY
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long numSpans = (long) Math.ceil((double) totalDays / spanDays);
        boolean skipBreakdown = numSpans > MAX_REPORT_SPANS;
        
        StringBuilder reportBuilder = new StringBuilder(skipBreakdown ? 512 : 16384);
        
        final String LINE = "========================================================================\n";
        final String HEADER_FORMAT = "%-30s %15s %15s\n";
        final String DETAIL_FORMAT = "%-30s %15.2f %15.2f\n";
        
        reportBuilder.append("PROFIT AND LOSS REPORT WITH SPAN BREAKDOWN\n");
        reportBuilder.append(String.format("Time Period: %s to %s | Span: %d Days\n\n", 
                                            startDate.toString(), endDate.toString(), spanDays));

        // --- SECTION A: Breakdown Report ---
        if (skipBreakdown) {
             reportBuilder.append(LINE);
             reportBuilder.append(String.format("NOTICE: The date range (%d days) with the chosen span (%d days) results in %d periods.\n", totalDays, spanDays, numSpans));
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
                LocalDate currentEnd = currentStart.plusDays(spanDays - 1);
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
        } // end of breakdown conditional
        
        // --- SECTION B: Total Summary --- (This is always included)
        // ... (rest of the P&L summary section remains the same) ...
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
        
        List<SalesTransaction> allTransactions = salesDAO.getTransactionsByDateRange(startDate, endDate);
        
        // CRITICAL CHECK FOR MEMORY
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long numSpans = (long) Math.ceil((double) totalDays / spanDays);
        boolean skipBreakdown = numSpans > MAX_REPORT_SPANS;

        // Use a smaller initial capacity if breakdown is skipped
        StringBuilder reportBuilder = new StringBuilder(skipBreakdown ? 512 : 16384);
        
        final String LINE = "========================================================================\n";
        final String HEADER_FORMAT = "%-15s %-40s %10s\n";

        reportBuilder.append("BEST SELLING REPORT WITH SPAN BREAKDOWN\n");
        reportBuilder.append(String.format("Time Period: %s to %s | Span: %d Days | Top %d\n\n", 
                                            startDate.toString(), endDate.toString(), spanDays, topN));
        
        // --- SECTION A: Breakdown Report ---
        if (skipBreakdown) {
             reportBuilder.append(LINE);
             reportBuilder.append(String.format("NOTICE: The date range (%d days) with the chosen span (%d days) results in %d periods.\n", totalDays, spanDays, numSpans));
             reportBuilder.append(String.format("To prevent OutOfMemoryError, the detailed breakdown is skipped.\n"));
             reportBuilder.append("Please choose a larger span or shorter date range.\n");
             reportBuilder.append(LINE);
        } else {
            LocalDate currentStart = startDate;
            
            while (!currentStart.isAfter(endDate)) {
                LocalDate currentEnd = currentStart.plusDays(spanDays - 1);
                if (currentEnd.isAfter(endDate)) {
                    currentEnd = endDate; 
                }
                
                String periodLabel = currentStart.isEqual(currentEnd) ? 
                                        currentStart.toString() : 
                                        currentStart.toString() + " to " + currentEnd.toString();
                
                reportBuilder.append(LINE);
                reportBuilder.append(String.format("PERIOD: %s\n", periodLabel));
                reportBuilder.append(LINE);

                // Filter transactions for the current span
                final LocalDate finalEnd = currentEnd;
                final LocalDate start = currentStart;
                List<SalesTransaction> spanTransactions = allTransactions.stream()
                    .filter(t -> t.getDateTime() != null) 
                    .filter(t -> t.getDateTime().toLocalDate().isAfter(start.minusDays(1)) && 
                               t.getDateTime().toLocalDate().isBefore(finalEnd.plusDays(1)))
                    .collect(Collectors.toList());

                // Calculate sales for this span
                Map<String, Integer> spanSalesByProduct = spanTransactions.stream()
                    .flatMap(t -> t.getSales().stream())
                    .collect(Collectors.groupingBy(
                        Sale::getProductID,
                        Collectors.summingInt(Sale::getQuantity)
                    ));

                // Sort, limit to top N, and format for this span
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
        } // end of breakdown conditional
        
        // --- SECTION B: Overall Summary --- (This is always included)
        reportBuilder.append("\n\n");
        reportBuilder.append(LINE);
        reportBuilder.append("B. OVERALL TOP " + topN + " PRODUCTS (FULL PERIOD)\n");
        reportBuilder.append(LINE);

        // Calculate sales for the overall period
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