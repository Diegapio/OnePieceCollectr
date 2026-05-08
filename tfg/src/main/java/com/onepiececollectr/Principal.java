package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;

public class Principal {

    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private Button btnToggle, btnDashboard, btnColeccion, btnMazos, btnMercado, btnEventos;

    private static StackPane staticContentArea;
    private final Map<String, Parent> vistaCache = new HashMap<>();
    private boolean sidebarExpanded = true;

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
    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        if (sidebarExpanded) {
            sidebar.setPrefWidth(190);
            btnToggle.setText("☰");
            btnDashboard.setText("🏠  Home");        btnDashboard.setPrefWidth(170);
            btnColeccion.setText("📦  Colección");   btnColeccion.setPrefWidth(170);
            btnMazos.setText("🃏  Mazos");            btnMazos.setPrefWidth(170);
            btnMercado.setText("🏪  Mercado");        btnMercado.setPrefWidth(170);
            btnEventos.setText("📅  Eventos");        btnEventos.setPrefWidth(170);
        } else {
            sidebar.setPrefWidth(50);
            btnToggle.setText("→");
            btnDashboard.setText("🏠"); btnDashboard.setPrefWidth(34);
            btnColeccion.setText("📦"); btnColeccion.setPrefWidth(34);
            btnMazos.setText("🃏");     btnMazos.setPrefWidth(34);
            btnMercado.setText("🏪");   btnMercado.setPrefWidth(34);
            btnEventos.setText("📅");   btnEventos.setPrefWidth(34);
        }
    }

    @FXML
    private void loadDashboard() {
        loadVista("/view/dashboard.fxml");
    }

    @FXML
    private void loadColeccion() {
        MazosController.mazoSeleccionado = null;
        vistaCache.remove("/view/coleccion.fxml");
        loadVista("/view/coleccion.fxml");
        if (ColeccionController.instancia != null) {
            ColeccionController.instancia.refrescar();
        }
    }

    @FXML
    private void loadMazos() {
        vistaCache.remove("/view/mazos.fxml");
        loadVista("/view/mazos.fxml");
    }

    @FXML
    private void loadMercado() {
        loadVista("/view/mercado.fxml");
    }

    @FXML
    private void loadEventos() {
        loadVista("/view/eventos.fxml");
    }
}
