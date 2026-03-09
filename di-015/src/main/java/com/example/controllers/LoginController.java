package com.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void login() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Login con: " + username);

        openMainApp();
    }

    @FXML
    private void register() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Registrar usuario: " + username);
    }

    private void openMainApp() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/principal.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 900, 600));
            stage.show();

            Stage current = (Stage) usernameField.getScene().getWindow();
            current.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}