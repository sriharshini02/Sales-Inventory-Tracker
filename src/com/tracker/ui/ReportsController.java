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
    }

    private void generateReport(String type) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            reportArea.setText("Error: Please select valid start and end dates.");
            return;
        }
        
        reportArea.clear();
        
        try {
            if (type.equals("P&L")) {
                lastGeneratedReport = reportService.generateProfitLossReport(
                    AuthenticationService.getActiveUser(), startDate, endDate
                );
            } else if (type.equals("BestSelling")) {
                int topN = Integer.parseInt(topNField.getText());
                lastGeneratedReport = reportService.generateBestSellingReport(
                    AuthenticationService.getActiveUser(), startDate, endDate, topN
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