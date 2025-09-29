package com.tracker.dao;

import com.tracker.model.Product;
import java.util.List;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles persistence for Product objects (the Inventory data store).
 */
public class ProductDAO {

    private static final String FILE_PATH = "data/products.dat";
    private List<Product> products;

    public ProductDAO() {
        this.products = loadProducts();
        
        // Ensure initial data exists if the file is empty (Bootstrap Example)
        if (this.products.isEmpty()) {
            bootstrapInitialProducts();
        }
    }
    
    private void bootstrapInitialProducts() {
        System.out.println("Bootstrapping initial products...");
        this.products.add(new Product("A101", "Espresso Machine", "Appliance", 150.00, 299.99, 10)); 
        this.products.add(new Product("A102", "Coffee Beans (Dark Roast)", "Food", 5.00, 12.50, 50));     
        this.products.add(new Product("A103", "Milk Frother", "Accessory", 20.00, 45.00, 25));     
        saveProducts();
    }
    
    public void saveProducts() {
        FileStorageUtil.saveData(this.products, FILE_PATH);
    }
    
    // Corresponds to 'checkIfExists' in Sequence Diagrams
    public Optional<Product> findById(String productId) {
        return products.stream()
                .filter(p -> p.getProductID().equalsIgnoreCase(productId))
                .findFirst();
    }
    
    // Corresponds to 'insertNewProduct' in Sequence Diagram
    public void add(Product product) {
        this.products.add(product);
        saveProducts();
    }
    
    // Corresponds to 'deleteProduct' in Sequence Diagram
    public void remove(String productId) {
        this.products.removeIf(p -> p.getProductID().equalsIgnoreCase(productId));
        saveProducts();
    }
    private List<Product> loadProducts() {
        // Assuming FileStorageUtil handles deserialization
        return FileStorageUtil.loadData(FILE_PATH);
    }
    public List<Product> getAll() {
        // Ensure we work with the latest list
        this.products = loadProducts(); 
        return new ArrayList<>(products);
    }
    public List<Product> getAllProducts() {
        // Ensure we work with the latest list
        this.products = loadProducts(); 
        return new ArrayList<>(products);
    }
    public Optional<Product> getProductById(String productID) {
        return getAllProducts().stream()
                .filter(p -> p.getProductID().equals(productID))
                .findFirst();
    }
}