package com.tracker.dao;

import com.tracker.model.Purchase;
import java.util.List;

/**
 * Handles persistence for Purchase objects (the Purchase DB store).
 */
public class PurchaseDAO {

    private static final String FILE_PATH = "data/purchases.dat";
    private List<Purchase> purchases;

    public PurchaseDAO() {
        this.purchases = FileStorageUtil.loadData(FILE_PATH);
    }
    
    public void addPurchase(Purchase purchase) {
        this.purchases.add(purchase);
        savePurchases();
    }

    public List<Purchase> getAllPurchases() {
        return purchases;
    }

    public void savePurchases() {
        FileStorageUtil.saveData(this.purchases, FILE_PATH);
    }
}