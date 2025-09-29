package com.tracker.main;

import javafx.application.Application; // Correct base class for JavaFX
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Sales & Inventory Tracker (Offline)");
        
        // Load the initial Login View
        showLoginView(); 
    }

    /**
     * Helper method to load and show the Login View.
     */
    public static void showLoginView() throws IOException {
        // NOTE: Using getClass().getResource() for robust path lookup
        Parent root = FXMLLoader.load(Main.class.getResource("/com/tracker/ui/LoginView.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Helper method to load and show the main application interface.
     */
    public static void showMainView() throws IOException {
        Parent root = FXMLLoader.load(Main.class.getResource("/com/tracker/ui/MainView.fxml"));
        Scene scene = new Scene(root, 1000, 700); // Set a standard size for the main app
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Standard main method that delegates control to the JavaFX Application class.
     */
    public static void main(String[] args) {
        launch(args); // This static method is inherited from javafx.application.Application
    }
}