package com.tracker.dao;

import com.tracker.model.SalesTransaction;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles persistence for SalesTransaction objects (the Sales DB store).
 */
public class SalesDAO {

    private static final String FILE_PATH = "data/sales.dat";
    private List<SalesTransaction> transactions;

    public SalesDAO() {
        this.transactions = FileStorageUtil.loadData(FILE_PATH);
    }
    
    // Corresponds to 'insertSaleRecord' in the Sequence Diagram
    public void addTransaction(SalesTransaction transaction) {
        this.transactions.add(transaction);
        saveTransactions();
    }

    // Corresponds to 'Fetch Sales Records' in the Sequence Diagram
    public List<SalesTransaction> getAllTransactions() {
        return transactions;
    }

    public void saveTransactions() {
        FileStorageUtil.saveData(this.transactions, FILE_PATH);
    }
    
    // Utility for report filtering
    public List<SalesTransaction> getTransactionsByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return transactions.stream()
                .filter(t -> !t.getDateTime().toLocalDate().isBefore(startDate) && 
                             !t.getDateTime().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
}