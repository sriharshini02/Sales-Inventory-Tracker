package com.tracker.model;

import java.io.Serializable;

/**
 * Represents a Supplier. Exists for UML traceability.
 * CRUD operations are not implemented for this lab's scope.
 */
public class Supplier implements Serializable {
    private static final long serialVersionUID = 1L;

    private String supplierID;
    private String name;
    private String contactInfo;

    public Supplier(String supplierID, String name, String contactInfo) {
        this.supplierID = supplierID;
        this.name = name;
        this.contactInfo = contactInfo;
    }

    // Getters and Setters (simplified, only getters shown for brevity)
    public String getSupplierID() { return supplierID; }
    public String getName() { return name; }
    public String getContactInfo() { return contactInfo; }
}