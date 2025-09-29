package com.tracker.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Corresponds to the 'Report' class in the diagram.
 * Stores report metadata and content.
 */
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reportID;
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportContent; 

    public Report(String reportType, LocalDate startDate, LocalDate endDate) {
        this.reportID = java.util.UUID.randomUUID().toString();
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getReportID() { return reportID; }
    public String getReportType() { return reportType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }

    // Corresponds to export(format) in the Class Diagram (FR-8)
    public String export(String format) {
        // In a real app, this would format and write to file (CSV/PDF)
        // For the lab, we return the formatted string content
        if (format.equalsIgnoreCase("CSV")) {
             return "CSV FORMAT:\n" + reportContent.replaceAll("\n", ", ");
        }
        return reportContent; // Simple text output
    }
}