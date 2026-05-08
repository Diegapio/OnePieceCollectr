package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class Principal {

    @FXML
    private StackPane contentArea;

    private static StackPane staticContentArea;
    private final Map<String, Parent> vistaCache = new HashMap<>();

    @FXML
    public void initialize() {
        staticContentArea = contentArea;
        loadVista("/view/dashboard.fxml");
    }

    public static void mostrarVista(Parent vista) {
        if (staticContentArea != null) {
            staticContentArea.getChildren().setAll(vista);
        }
    }

    private void loadVista(String fxml) {
        try {
            Parent view = vistaCache.computeIfAbsent(fxml, key -> {
                try {
                    return new FXMLLoader(getClass().getResource(key)).load();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
            if (view != null) staticContentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            Login.registrarEnLog("ERROR CARGANDO VISTA " + fxml + ": " + e.getMessage());
        }
    }

    @FXML
    public void loadDashboard(ActionEvent event) {
        loadVista("/view/dashboard.fxml");
    }

    @FXML
    private void loadColeccion() {
        MazosController.mazoSeleccionado = null;
        loadVista("/view/coleccion.fxml");
        if (ColeccionController.instancia != null) {
            ColeccionController.instancia.refrescar();
        }
    }

    @FXML
    public void loadMazos(ActionEvent event) {
        loadVista("/view/mazos.fxml");
    }

    @FXML
    public void loadMercado(ActionEvent event) {
        loadVista("/view/mercado.fxml");
    }

    @FXML
    public void loadEventos(ActionEvent event) {
        loadVista("/view/eventos.fxml");
    }
}