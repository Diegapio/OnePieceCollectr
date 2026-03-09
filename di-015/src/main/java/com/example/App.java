package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

       FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/view/login.fxml")
);

        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("One Piece Collectr");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}