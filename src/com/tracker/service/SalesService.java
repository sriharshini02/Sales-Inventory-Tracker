package com.tracker.service;

import com.tracker.dao.SalesDAO;
import com.tracker.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Executes the business logic for the Record Sale use case.
 * Corresponds to 'SalesManager' in the Sequence Diagram.
 */
public class SalesService {

    private final SalesDAO salesDAO;
    private final InventoryService inventoryService; 

    public SalesService(InventoryService inventoryService) {
        this.salesDAO = new SalesDAO();
        this.inventoryService = inventoryService;
    }

    /**
     * Simulates the Record Sale Sequence Diagram logic.
     * Processes a list of sales items within a single transaction.
     */
    public String recordSaleTransaction(User user, List<SaleRequest> items, String paymentMethod) {
        // Enforce role-based access control (Staff or ShopKeeper)
        if (user == null || !(user.getRole().equals("STAFF") || user.getRole().equals("SHOPKEEPER"))) {
            return "Access Denied: Only Staff and ShopKeeper can record sales.";
        }

        SalesTransaction transaction = new SalesTransaction(UUID.randomUUID().toString(), paymentMethod);
        
        // 1. Pre-Check for Stock and existence
        for (SaleRequest item : items) {
            String productId = item.getProductId();
            int quantity = item.getQuantity();

            // checkIfExists (via inventoryService -> ProductDAO)
            Product product = inventoryService.getProductById(productId)
                    .orElse(null);
            
            if (product == null) {
                // ALT [product not found]
                return "Error: Product ID " + productId + " not found.";
            }

            // checkStock (via InventoryService)
            if (product.getStockQuantity() < quantity) {
                // ALT [stock < quantity]
                return "Error: Insufficient stock for " + product.getName() + 
                       ". Available: " + product.getStockQuantity();
            }
        }
        
        // 2. Process Transaction (Commit and Update)
        for (SaleRequest item : items) {
            Product product = inventoryService.getProductById(item.getProductId()).get(); // Must exist due to pre-check
            
            // a) updateStock(productID, stock-quantity)
            product.updateStock(-item.getQuantity()); 
            
            // b) Create Sale Item for the Transaction Record (Saves historical cost/price)
            Sale sale = new Sale(
                UUID.randomUUID().toString(),
                item.getProductId(),
                item.getQuantity(),
                product.getSellingPrice(), // Use current selling price
                product.getCostPrice()     // Use current cost price
            );
            transaction.addSale(sale);
        }

        // 3. Persist changes
        inventoryService.persistChanges(); // Save updated stock levels
        salesDAO.addTransaction(transaction); // insertSaleRecord
        
        return "Sale recorded successfully. Total: " + transaction.getCalculatedTotal();
    }
    
    public List<SalesTransaction> viewSalesHistory() {
        return salesDAO.getAllTransactions();
    }

    // Simple DTO for sales request data
    public static class SaleRequest {
        private final String productId;
        private final int quantity;

        public SaleRequest(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
    }
}