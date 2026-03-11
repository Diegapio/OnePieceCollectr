package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class Principal {

    Login login = new Login();
    @FXML
    private StackPane contentArea; 

    @FXML
    public void loadDashboard(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                login.registrarEnLog("ERROR CARGANDO DASHBOARD: " + e.getMessage());
                login.mostrarAlerta("Error", "No se pudo cargar el dashboard.");
            }
        
    }

    @FXML
    public void loadColeccion(ActionEvent event) {
         try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/coleccion.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                login.registrarEnLog("ERROR CARGANDO A COLECCION: " + e.getMessage());
                login.mostrarAlerta("Error", "No se pudo cargar la colección.");
            }
    }

    @FXML
    public void loadMazos(ActionEvent event) {
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mazos.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                login.registrarEnLog("ERROR CARGANDO A MAZOS: " + e.getMessage());
                login.mostrarAlerta("Error", "No se pudo cargar los mazos.");
            }
    }

    @FXML
    public void loadMercado(ActionEvent event) {
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mercado.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                login.registrarEnLog("ERROR CARGANDO AL MERCADO: " + e.getMessage());
                login.mostrarAlerta("Error", "No se pudo cargar el mercado.");
            }
    }

    @FXML
    public void loadEventos(ActionEvent event) {
        try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/eventos.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                login.registrarEnLog("ERROR CARGANDO A EVENTOS: " + e.getMessage());
                login.mostrarAlerta("Error", "No se pudo cargar los eventos.");
            }
    }
}