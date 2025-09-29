package com.tracker.service;

import com.tracker.dao.ProductDAO;
import com.tracker.model.Product;
import com.tracker.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Executes the business logic for Add/Update/Remove Product and stock viewing.
 * Corresponds to 'ProductManager' in Sequence Diagrams.
 */
public class InventoryService {

    private final ProductDAO productDAO;

    public InventoryService() {
        this.productDAO = new ProductDAO();
    }
    
    // --- Use Case: Add Product ---
    // Implements the logic from the "Add Product" sequence diagram
    public boolean addOrUpdateProduct(User user, Product newProduct, int initialStock) {
        if (user == null || !user.getRole().equals("SHOPKEEPER")) {
            System.out.println("Access Denied: Only ShopKeeper can add/update products.");
            return false;
        }

        // 1. checkIfExists (via DAO)
        Optional<Product> existingProductOpt = productDAO.findById(newProduct.getProductID());

        if (existingProductOpt.isPresent()) {
            // ALT [product exists] -> updateStock
            Product existingProduct = existingProductOpt.get();
            System.out.println("Product exists. Updating stock and details.");
            
            // Update mutable details
            existingProduct.setName(newProduct.getName());
            existingProduct.setCategory(newProduct.getCategory());
            existingProduct.setCostPrice(newProduct.getCostPrice());
            existingProduct.setSellingPrice(newProduct.getSellingPrice());
            
            // Update stock quantity (This assumes the flow is for receiving new stock/initial stock)
            existingProduct.updateStock(initialStock); 
            
        } else {
            // ALT [new product] -> insertNewProduct
            System.out.println("New product. Adding to inventory.");
            newProduct.updateStock(initialStock); // Set initial stock
            productDAO.add(newProduct);
        }
        
        productDAO.saveProducts();
        return true;
    }

    // --- Use Case: Remove Product ---
    // Implements the logic from the "Remove Product" sequence diagram
    public String removeProduct(User user, String productId) {
        if (user == null || !user.getRole().equals("SHOPKEEPER")) {
            return "Access Denied: Only ShopKeeper can remove products.";
        }
        
        // 1. checkIfExists (via DAO)
        Optional<Product> productOpt = productDAO.findById(productId);

        if (productOpt.isEmpty()) {
            // ALT [product not found]
            return "Error: product does not exist.";
        }
        
        Product product = productOpt.get();
        
        // ALT [product found] -> checkStock
        if (product.getStockQuantity() > 0) {
            // ALT [stock > 0]
            return "Error: cannot remove, stock available (" + product.getStockQuantity() + " units).";
        }

        // ALT [stock = 0] -> deleteProduct
        productDAO.remove(productId);
        return "Product removed successfully.";
    }
    
    // --- Use Case: View Current Stock ---
    // Implements the logic from the "View Current Stock" sequence diagram
    public List<Product> viewCurrentStock() {
        // Simple fetch Stock Data
        return productDAO.getAll();
    }
    
    // Utility method for other services (e.g., SalesService)
    public Optional<Product> getProductById(String productId) {
        return productDAO.findById(productId);
    }
    
    // Utility method to save changes made to a product (e.g., stock update after sale/purchase)
    public void persistChanges() {
        productDAO.saveProducts();
    }
}