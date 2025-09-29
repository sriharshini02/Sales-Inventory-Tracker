package com.tracker.service;

import com.tracker.dao.PurchaseDAO;
import com.tracker.model.Product;
import com.tracker.model.Purchase;
import com.tracker.model.User;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Executes the business logic for the Record Purchase use case.
 */
public class PurchaseService {

    private final PurchaseDAO purchaseDAO;
    private final InventoryService inventoryService;

    public PurchaseService(InventoryService inventoryService) {
        this.purchaseDAO = new PurchaseDAO();
        this.inventoryService = inventoryService;
    }

    /**
     * Records a new purchase, updating stock and product cost price.
     * Only ShopKeeper can perform this action.
     */
    public String recordPurchase(User user, String productId, int quantity, double costPrice, String supplierName) {
        // Enforce role-based access control
        if (user == null || !user.getRole().equals("SHOPKEEPER")) {
            return "Access Denied: Only ShopKeeper can record purchases.";
        }
        
        // Input validation
        if (quantity <= 0) {
            return "Error: Quantity must be greater than zero.";
        }
        if (costPrice <= 0) {
            return "Error: Cost price must be positive.";
        }

        // 1. Find Product
        Product product = inventoryService.getProductById(productId).orElse(null);
        
        if (product == null) {
            // ALT [product not found]
            return "Error: Product ID " + productId + " not found in inventory. Add it first.";
        }

        // 2. Update Product/Inventory (Adds Stock)
        product.updateStock(quantity);
        // Important: Update product cost price (FR-3 includes recording cost price)
        product.setCostPrice(costPrice); 
        
        // 3. Record Purchase Transaction
        Purchase purchase = new Purchase(
            UUID.randomUUID().toString(),
            productId,
            quantity,
            costPrice,
            LocalDate.now(),
            supplierName 
        );
        
        // 4. Persist
        inventoryService.persistChanges(); // Save updated stock/price
        purchaseDAO.addPurchase(purchase); // Save purchase record
        
        return "Purchase recorded successfully. Stock updated.";
    }
}