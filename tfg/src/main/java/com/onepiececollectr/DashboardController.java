package com.onepiececollectr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DashboardController {

    @FXML private Label cardsCount;
    @FXML private Label decksCount;
    @FXML private Label eventsCount;
    @FXML private Label progressPercent; 
    @FXML private ProgressBar progressBar; 
    @FXML private Label nextEventLabel;

    @FXML
    public void initialize() {
        if (Login.sesionUsuario != null) {
            int idUsuario = Login.sesionUsuario.getId();
            actualizarEstadisticas(idUsuario);
        }
    }

    private void actualizarEstadisticas(int idUsuario) {
        int cartasPoseidas = 0;
        int totalCartasApp = App.todasLasCartas.size(); 
        String sql = "SELECT COUNT(DISTINCT id_carta) FROM coleccion WHERE id_usuario = ?";

        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                cartasPoseidas = rs.getInt(1);
            }

            // 1. Mostrar total de cartas
            cardsCount.setText(String.valueOf(cartasPoseidas));
            
            // 2. Calcular porcentaje y barra
            if (totalCartasApp > 0) {
                double fraccion = (double) cartasPoseidas / totalCartasApp;
                
                if (progressPercent != null) {
                    progressPercent.setText(String.format("%.2f%%", fraccion * 100));
                }
                if (progressBar != null) {
                    progressBar.setProgress(fraccion);
                }
            }

            
            int numMazos = MazosController.getMisMazos().size();
            int numEventos = EventosController.getListaEventos().size();

            decksCount.setText(String.valueOf(numMazos));
            eventsCount.setText(String.valueOf(numEventos));
            if (nextEventLabel != null) {
    if (numEventos > 0) {
        // Obtenemos el primer evento de la lista
        Event proximo = EventosController.getListaEventos().get(0);
    
        nextEventLabel.setText(proximo.getName() + " [" + proximo.getDate() + "]");
 
        nextEventLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
    } else {
        nextEventLabel.setText("Sin eventos programados");
        nextEventLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");
    }
}
    }catch (SQLException e) {
            e.printStackTrace();
        }
         
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        try {
            // Si quieres volver al Dashboard desde otra pantalla, 
            // esto está bien, pero recuerda que el initialize() ya lo hace.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            Principal.mostrarVista(root);
        } catch (Exception e) {
            System.err.println("Error al volver al principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onCardHover() {}
    @FXML private void onCardExit() {}
}