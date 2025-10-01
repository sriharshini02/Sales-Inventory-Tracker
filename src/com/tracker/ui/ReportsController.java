package com.tracker.ui;

import com.tracker.dao.SalesDAO;
import com.tracker.dao.UserDAO; 
import com.tracker.model.Report;
import com.tracker.model.User;
import com.tracker.service.AuthenticationService;
import com.tracker.service.ReportExporter;
import com.tracker.service.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

public class ReportsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField topNField;
    @FXML private TextField spanField;
    @FXML private TextArea reportArea;

    // Services
    private final SalesDAO salesDAO = new SalesDAO();
    private final ReportService reportService = new ReportService(salesDAO);
    private final ReportExporter reportExporter = new ReportExporter(); // 1. Initialized ReportExporter
    
    // State Variables for Export
    private Report lastGeneratedReport;
    private User currentUser; 
    
    // Last used parameters (CRITICAL for export)
    private String lastReportType; // Changed from 'Report' to 'String' for simplicity
    private LocalDate lastStartDate;
    private LocalDate lastEndDate;
    private int lastSpanDays;
    private int lastTopN;

    @FXML
    public void initialize() {
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        topNField.setText("5");
        spanField.setText("7"); // Default span of 7 days
        
        // Ensure currentUser is set if needed for permission checks in export
        this.currentUser = AuthenticationService.getActiveUser();
    }

    private void generateReport(String type) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        int spanDays = 0;
        int topN = 0;

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
                // 2a. Store parameters for P&L
                this.lastReportType = "P&L";
                this.lastStartDate = startDate;
                this.lastEndDate = endDate;
                this.lastSpanDays = spanDays;
                this.lastTopN = 0; // Not used for P&L

            } else if (type.equals("BestSelling")) {
                topN = Integer.parseInt(topNField.getText());
                spanDays = Integer.parseInt(spanField.getText());
                if (spanDays < 1) {
                    reportArea.setText("Error: Span must be a positive number of days.");
                    return;
                }
                lastGeneratedReport = reportService.generateBestSellingReport(
                    AuthenticationService.getActiveUser(), startDate, endDate, topN, spanDays
                );
                // 2b. Store parameters for Best Selling
                this.lastReportType = "BestSelling";
                this.lastStartDate = startDate;
                this.lastEndDate = endDate;
                this.lastSpanDays = spanDays;
                this.lastTopN = topN;
                
            } else {
                return;
            }

            if (lastGeneratedReport.getReportType().equals("Access Denied")) {
                reportArea.setText("Access Denied: You must be a ShopKeeper to generate reports.");
                // Clear state if access is denied
                this.lastGeneratedReport = null; 
            } else {
                reportArea.setText(lastGeneratedReport.getReportType() + " Report\n" + lastGeneratedReport.getReportContent());
            }
            
        } catch (NumberFormatException e) {
            reportArea.setText("Error: Invalid number for Top N or Span value.");
            this.lastGeneratedReport = null; // Clear state on error
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
        if (lastGeneratedReport == null || lastReportType == null) {
            reportArea.setText("Error: Please generate a report first before exporting.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        String reportName = lastReportType.replace(" ", "_"); 

        fileChooser.setTitle("Save " + lastReportType + " Report as CSV");
        fileChooser.setInitialFileName(reportName + "_" + lastStartDate + "_to_" + lastEndDate + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) reportArea.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                String csvContent;
                
                // 1. Generate Structured Data and CSV Content
                if (lastReportType.equals("P&L")) {
                    csvContent = reportExporter.exportPnlToCsv(
                        reportService.getPnlRecords(currentUser, lastStartDate, lastEndDate, lastSpanDays)
                    );
                } else if (lastReportType.equals("BestSelling")) {
                    csvContent = reportExporter.exportBestSellingToCsv(
                        reportService.getBestSellingRecords(currentUser, lastStartDate, lastEndDate, lastTopN, lastSpanDays)
                    );
                } else {
                    reportArea.setText("Error: Unknown report type. Cannot export.");
                    return;
                }

                // 2. Write CSV Content to the chosen file using standard Java Files utility
                Files.write(file.toPath(), csvContent.getBytes());

                // 3. Success Feedback
                // Restore original report text area content (in case of an error message)
                reportArea.setText(lastGeneratedReport.getReportType() + " Report\n" + lastGeneratedReport.getReportContent());
                // In a real application, you would use a JavaFX Alert here.
                System.out.println("Success: " + lastReportType + " Report exported successfully to: " + file.getAbsolutePath());

            } catch (IOException e) {
                // Handle file writing or export processing errors
                reportArea.setText("Failed to export CSV: " + e.getMessage());
            } catch (Exception e) {
                // Handle unexpected errors during data retrieval/export
                reportArea.setText("An unexpected error occurred during CSV export: " + e.getMessage());
            }
        }
    }
}