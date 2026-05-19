package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class Principal {

    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private Button btnToggle;       // flotante, visible cuando sidebar oculto
    @FXML private Button btnToggleInner;  // dentro del sidebar, visible cuando está abierto
    @FXML private Button btnDashboard, btnColeccion, btnMazos, btnMercado, btnEventos;
    @FXML private Button btnMinimize, btnMaximize, btnClose;
    @FXML private HBox windowControls;
    MazosController mazos = new MazosController();

    MarketController mercado = new MarketController();

    private static StackPane staticContentArea;
    private final Map<String, Parent> vistaCache = new HashMap<>();
    private boolean sidebarExpanded = true;

    private double dragOffsetX, dragOffsetY;

    private static final String BTN_STYLE =
        "-fx-background-color: #e8c96d;" +
        "-fx-text-fill: #0d1b2a;" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 10;" +
        "-fx-padding: 9 14;" +
        "-fx-alignment: CENTER_LEFT;" +
        "-fx-font-size: 13px;" +
        "-fx-cursor: hand;" +
        "-fx-border-color: transparent;";

    private static final String BTN_HOVER =
        "-fx-background-color: #c9a84c;" +
        "-fx-text-fill: #0d1b2a;" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 10;" +
        "-fx-padding: 9 14;" +
        "-fx-alignment: CENTER_LEFT;" +
        "-fx-font-size: 13px;" +
        "-fx-cursor: hand;" +
        "-fx-border-color: transparent;";

    private static final String TOGGLE_STYLE =
        "-fx-background-color: transparent;" +
        "-fx-text-fill: #0d1b2a;" +
        "-fx-font-size: 18px; -fx-font-weight: bold;" +
        "-fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 8;";

    @FXML
    public void initialize() {
        staticContentArea = contentArea;

        // Estilos de nav buttons vía código (garantizado sobre modena.css)
        for (Button b : new Button[]{btnDashboard, btnColeccion, btnMazos, btnMercado, btnEventos}) {
            b.setStyle(BTN_STYLE);
            b.setOnMouseEntered(e -> b.setStyle(BTN_HOVER));
            b.setOnMouseExited(e -> b.setStyle(BTN_STYLE));
        }
        btnToggleInner.setStyle(TOGGLE_STYLE);

        // CSS a nivel de escena para scrollbars y TextArea/DatePicker
        sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                String css = getClass().getResource("/view/sidebar.css").toExternalForm();
                if (!newScene.getStylesheets().contains(css))
                    newScene.getStylesheets().add(css);
            }
        });

        // Estilos de los controles de ventana
        String winBtn =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #7fb3d3;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2 7;" +
            "-fx-background-radius: 5;";
        for (Button b : new Button[]{btnMinimize, btnMaximize}) {
            b.setStyle(winBtn);
            b.setOnMouseEntered(e -> b.setStyle(winBtn + "-fx-background-color: #1a2c42;"));
            b.setOnMouseExited(e -> b.setStyle(winBtn));
        }
        btnClose.setStyle(winBtn);
        btnClose.setOnMouseEntered(e -> btnClose.setStyle(winBtn + "-fx-background-color: #c0392b; -fx-text-fill: white;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle(winBtn));

        // Drag de ventana arrastrando desde el área de controles
        windowControls.setOnMousePressed(e -> {
            Stage stage = (Stage) windowControls.getScene().getWindow();
            dragOffsetX = e.getScreenX() - stage.getX();
            dragOffsetY = e.getScreenY() - stage.getY();
        });
        windowControls.setOnMouseDragged(e -> {
            Stage stage = (Stage) windowControls.getScene().getWindow();
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });

        loadVista("/view/dashboard.fxml");
        toggleSidebar(); // arranca plegado
    }

    public static void mostrarVista(Parent vista) {
        if (staticContentArea != null)
            staticContentArea.getChildren().setAll(vista);
    }

    private void loadVista(String fxml) {
        try {
            Parent view = vistaCache.computeIfAbsent(fxml, key -> {
                try { return new FXMLLoader(getClass().getResource(key)).load(); }
                catch (Exception e) { e.printStackTrace(); return null; }
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
        // Sidebar: visible/managed alterna
        sidebar.setVisible(sidebarExpanded);
        sidebar.setManaged(sidebarExpanded);
        // Botón flotante: aparece solo cuando el sidebar está oculto
        btnToggle.setVisible(!sidebarExpanded);
        btnToggle.setManaged(!sidebarExpanded);
    }

    @FXML private void loadDashboard() { loadVista("/view/dashboard.fxml"); }

    @FXML
    private void loadColeccion() {
        MazosController.mazoSeleccionado = null;
        vistaCache.remove("/view/coleccion.fxml");
        loadVista("/view/coleccion.fxml");
        if (ColeccionController.instancia != null)
            ColeccionController.instancia.refrescar();
        mercado.modoSeleccionMercado = false;
        mazos.mazoSeleccionado = null;
    }

    @FXML
    private void loadMazos() {
        vistaCache.remove("/view/mazos.fxml");
        loadVista("/view/mazos.fxml");
    }

    @FXML private void loadMercado() { loadVista("/view/mercado.fxml"); }
    @FXML private void loadEventos() { loadVista("/view/eventos.fxml"); }

    @FXML private void minimizeWindow() {
        ((Stage) contentArea.getScene().getWindow()).setIconified(true);
    }

    @FXML private void maximizeWindow() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML private void closeWindow() {
        ((Stage) contentArea.getScene().getWindow()).close();
    }
}
