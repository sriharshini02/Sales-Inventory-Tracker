package com.tracker.dao;

import com.tracker.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * Handles persistence for Product objects (the Inventory data store).
 */
public class ProductDAO {

    private static final String FILE_PATH = "data/products.dat";
    private List<Product> products;

    public ProductDAO() {
        this.products = FileStorageUtil.loadData(FILE_PATH);
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

    // Corresponds to 'Fetch Stock Data' and 'View Current Stock'
    public List<Product> getAll() {
        return products;
    }
}