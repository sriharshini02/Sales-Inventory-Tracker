package com.tracker.service;

import com.tracker.dao.ProductDAO;
import java.util.ArrayList;
import com.tracker.model.Product;
import com.tracker.model.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
 // In com.tracker.service.InventoryService.java

 // ... existing code ...

 /**
  * Handles the 'Add/Update Product' use case.
  */
 // In com.tracker.service.InventoryService.java

    public boolean addOrUpdateProduct(User user, Product newProduct, int stockChange) {
            
        // 1. Get existing product by ID
        Optional<Product> existingProductById = productDAO.getProductById(newProduct.getProductID());
        
        // --- COMMON NAME UNIQUENESS CHECK ---
        
        // Check if a product with the new name already exists in the system.
        Optional<Product> existingProductByName = productDAO.getProductByName(newProduct.getName());

        if (existingProductByName.isPresent()) {
            Product conflictProduct = existingProductByName.get();
            
            // This is the core fix: Allow the operation ONLY if the name conflict 
            // is with the product being updated itself.
            boolean isSelfConflict = existingProductById.isPresent() 
                                   && conflictProduct.getProductID().equals(newProduct.getProductID());

            if (!isSelfConflict) {
                // Error: Name conflict with a DIFFERENT product ID. Block the operation.
                System.err.println("Error: A product with the name '" + newProduct.getName() + "' already exists (ID: " + conflictProduct.getProductID() + ").");
                return false;
            }
            // If isSelfConflict is true, it means the user is updating product 'A' and keeping name 'X', which is fine.
        }
        
        // 2. Logic for NEW PRODUCT (ID is new)
        if (existingProductById.isEmpty()) { 
            
            // If we reached here, the name is unique (checked above). Insert it.
            try {
                // Set the initial stock from the form, if the ID is new
            	newProduct.setStockQuantity(stockChange); 
                
                productDAO.insertNewProduct(newProduct); 
                return true;
            } catch (Exception e) {
                System.err.println("Error inserting new product: " + e.getMessage());
                return false;
            }
        } 
        
        // 3. Logic for EXISTING PRODUCT (UPDATE)
        else {
            Product existingProduct = existingProductById.get();
            
            try {
                // A) Update stock: Use the explicit 'stockChange' amount provided.
                existingProduct.updateStock(stockChange); 
                
                // B) Update price and name/category details from the newProduct object
                existingProduct.updatePrice(newProduct.getCostPrice(), newProduct.getSellingPrice());
                existingProduct.setName(newProduct.getName());        // Assume setter exists
                existingProduct.setCategory(newProduct.getCategory()); // Assume setter exists

                // C) Persist changes
                productDAO.saveProductChanges(existingProduct); 
                return true;
            } catch (Exception e) {
                System.err.println("Error updating product: " + e.getMessage());
                return false;
            }
        }
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
    public Optional<Product> getProductByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // FIX: Call the non-static method on the instance variable 'productDAO'
        return this.productDAO.getAllProducts().stream() 
                .filter(p -> p.getName().trim().equalsIgnoreCase(name.trim()))
                .findFirst();
    }
    
    /**
     * Searches the inventory for products matching the given name query.
     * @param query The partial or full product name to search for.
     * @return A list of matching Product objects.
     */
    public List<Product> searchProductsByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(); // Return empty list if no query
        }
        
        String lowerCaseQuery = query.trim().toLowerCase();
        
        // 1. Get all products (using the DAO's method)
        List<Product> allProducts = productDAO.getAllProducts(); 
        
        // 2. Filter the list by name
        return allProducts.stream()
            .filter(p -> p.getName().toLowerCase().contains(lowerCaseQuery))
            .collect(Collectors.toList());
    }
    
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
}