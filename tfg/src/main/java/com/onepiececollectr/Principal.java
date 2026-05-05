package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;

public class Principal {

    // Instancia de Login para usar el registro de logs y alertas
    Login login = new Login();
    
    @FXML
    private StackPane contentArea; 

    // Variable estática para que otros controladores (como el de detalle) puedan cambiar la vista
    private static StackPane staticContentArea;

    @FXML
    public void initialize() {
        // Inicializamos la referencia estática
        staticContentArea = contentArea;
        
        // CARGA INICIAL: Asegúrate de que el archivo sea dashboard.fxml (todo minúsculas)
        loadVista("/view/dashboard.fxml");
    }

    /**
     * Método estático para cambiar el contenido del StackPane desde cualquier clase.
     */
    public static void mostrarVista(Parent vista) {
        if (staticContentArea != null) {
            staticContentArea.getChildren().setAll(vista);
        }
    }

    /**
     * Método interno para cargar un FXML y ponerlo en el área central.
     */
    private void loadVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            
            if (staticContentArea != null) {
                staticContentArea.getChildren().setAll(view);
            }
        } catch (Exception e) {
            // Si falla, registramos el error exacto para saber qué archivo da problemas
            System.err.println("Error cargando: " + fxml);
            e.printStackTrace();
            login.registrarEnLog("ERROR CARGANDO VISTA " + fxml + ": " + e.getMessage());
            login.mostrarAlerta("Error", "No se pudo cargar la vista: " + fxml);
        }
    }

    @FXML 
    public void loadDashboard(ActionEvent event) { 
        loadVista("/view/dashboard.fxml"); 
    }

    
    @FXML
    private void loadColeccion() {  
    MazosController.mazoSeleccionado = null; 

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/coleccion.fxml"));
        Principal.mostrarVista(loader.load());
    } catch (Exception e) {
        e.printStackTrace();
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