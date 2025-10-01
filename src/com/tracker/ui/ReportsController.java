package com.tracker.ui;

import com.tracker.dao.SalesDAO;
import com.tracker.model.Report;
import com.tracker.service.AuthenticationService;
import com.tracker.service.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.time.LocalDate;

public class ReportsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField topNField;
    @FXML private TextField spanField;
    @FXML private TextArea reportArea;

    // Services needed
    private final SalesDAO salesDAO = new SalesDAO(); // ReportService needs a DAO dependency
    private final ReportService reportService = new ReportService(salesDAO);
    
    private Report lastGeneratedReport; // To hold the report for export

    @FXML
    public void initialize() {
        // Set default dates for convenience
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        topNField.setText("5");
        spanField.setText("7"); // Default span of 7 days
    }

    private void generateReport(String type) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        int spanDays = 0;
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            reportArea.setText("Error: Please select valid start and end dates.");
            return;
        }
        
        reportArea.clear();
        
        try {
            if (type.equals("P&L")) {
            	spanDays = Integer.parseInt(spanField.getText());
                if (spanDays < 1) {
                    reportArea.setText("Error: Span must be a positive number of days.");
                    return;
                }
                lastGeneratedReport = reportService.generateProfitLossReport(
                    AuthenticationService.getActiveUser(), startDate, endDate, spanDays
                );
            } else if (type.equals("BestSelling")) {
                int topN = Integer.parseInt(topNField.getText());
                spanDays = Integer.parseInt(spanField.getText());
                lastGeneratedReport = reportService.generateBestSellingReport(
                    AuthenticationService.getActiveUser(), startDate, endDate, topN, spanDays
                );
            } else {
                return;
            }

            if (lastGeneratedReport.getReportType().equals("Access Denied")) {
                reportArea.setText("Access Denied: You must be a ShopKeeper to generate reports.");
            } else {
                reportArea.setText(lastGeneratedReport.getReportType() + " Report\n" + lastGeneratedReport.getReportContent());
            }
            
        } catch (NumberFormatException e) {
            reportArea.setText("Error: Invalid number for Top N value.");
        }
    }

    @FXML
    public void handleProfitLossReport() {
        generateReport("P&L");
    }

    @FXML
    public void handleBestSellingReport() {
        generateReport("BestSelling");
    }
    
    @FXML
    public void handleExportReport() {
        if (lastGeneratedReport == null || lastGeneratedReport.getReportContent() == null) {
            reportArea.setText("Error: Please generate a report first before exporting.");
            return;
        }
        
        // Simple export simulation (FR-8: Export Report)
        String exportedContent = lastGeneratedReport.export("CSV");
        reportArea.setText("--- EXPORTED CONTENT (CSV FORMAT) ---\n" + exportedContent);
    }
}