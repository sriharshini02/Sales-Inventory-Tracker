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
    private static final String PRODUCT_FILE = "products.dat"; 
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
    /**
     * Maps to productDAO.insertNewProduct(newProduct) in the Service Layer.
     * Adds a new product to the list and saves changes.
     */
    public void insertNewProduct(Product product) {
        this.products.add(product);
        saveProducts(); // Persist the change
    }

    /**
     * Maps to productDAO.saveProductChanges(existingProduct) in the Service Layer.
     * Replaces the old product object with the updated one and saves changes.
     */
    public void saveProductChanges(Product updatedProduct) {
        // 1. Remove the old version of the product using its ID
        this.products.removeIf(p -> p.getProductID().equals(updatedProduct.getProductID()));
        
        // 2. Add the updated version
        this.products.add(updatedProduct);
        
        // 3. Persist the change
        saveProducts(); 
    }
    
    private List<Product> loadProducts() {
        // Assuming FileStorageUtil handles deserialization from FILE_PATH
        // We ensure the list is always fresh before searching/updating
        List<Product> loadedList = FileStorageUtil.loadData(FILE_PATH);
        return loadedList != null ? loadedList : new ArrayList<>();
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
        // Ensure the in-memory list is up-to-date before searching
        this.products = loadProducts(); 
        
        return this.products.stream()
                .filter(p -> p.getProductID().equals(productID))
                .findFirst();
    }
    public Optional<Product> getProductByName(String name) {
        // Rely on the in-memory list 'this.products' which should be updated 
        // by insertNewProduct and saveProductChanges.
        
        return this.products.stream() // Use 'this.products'
            .filter(p -> p.getName().equalsIgnoreCase(name))
            .findFirst();
    }
}