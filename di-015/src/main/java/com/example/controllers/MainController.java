package com.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadDashboard();
    }

    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + page));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadDashboard() {
        loadPage("dashboard.fxml");
    }

    @FXML
    private void loadColeccion() {
        loadPage("coleccion.fxml");
    }

    @FXML
    private void loadMazos() {
        loadPage("mazos.fxml");
    }

    @FXML
    private void loadMercado() {
        loadPage("mercado.fxml");
    }

    @FXML
    private void loadEventos() {
        loadPage("eventos.fxml");
    }
}
