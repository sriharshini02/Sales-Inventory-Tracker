package com.tracker.main;

import java.io.IOException;

import javax.ws.rs.core.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

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
        // We will create the LoginView.fxml in the next step
        Parent root = FXMLLoader.load(Main.class.getResource("/com/tracker/ui/LoginView.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Helper method to load and show the main application interface.
     */
    public static void showMainView() throws IOException {
        // We will create the MainView.fxml later
        Parent root = FXMLLoader.load(Main.class.getResource("/com/tracker/ui/MainView.fxml"));
        Scene scene = new Scene(root, 1000, 700); // Set a standard size for the main app
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

	private static void launch(String[] args) {
		// TODO Auto-generated method stub
		
	}
}