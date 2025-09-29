package com.tracker.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading and writing data to local files using Java Serialization.
 * This implements the 'Database' lifeline for your offline-only application (NFR-1).
 */
public class FileStorageUtil {

    // Prevent instantiation
    private FileStorageUtil() {}

    /**
     * Reads a list of objects from a specified file path.
     * @param <T> The type of objects in the list.
     * @param filePath The local path to the data file (e.g., "data/products.dat").
     * @return List of objects, or an empty list if file not found or error occurs.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> loadData(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            // If file doesn't exist, return an empty list
            return new ArrayList<>();
        }

        try (FileInputStream fileIn = new FileInputStream(file);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            
            // The file is expected to contain a serialized List
            return (List<T>) objectIn.readObject();

        } catch (IOException e) {
            System.err.println("Error reading data from " + filePath + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found during deserialization: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Writes a list of objects to a specified file path.
     * @param <T> The type of objects in the list.
     * @param data The list of objects to save.
     * @param filePath The local path to the data file.
     */
    public static <T> void saveData(List<T> data, String filePath) {
        try {
            File file = new File(filePath);
            // Ensure the directory 'data/' exists
            file.getParentFile().mkdirs();

            try (FileOutputStream fileOut = new FileOutputStream(file);
                 ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
                
                objectOut.writeObject(data);
                // System.out.println("Data successfully saved to " + filePath); // Optional feedback
                
            }
        } catch (IOException e) {
            System.err.println("Error writing data to " + filePath + ": " + e.getMessage());
        }
    }
}