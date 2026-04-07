package com.onepiececollectr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DashboardController {

    @FXML private Label cardsCount;
    @FXML private Label decksCount;
    @FXML private Label eventsCount;
    @FXML private Label progressPercent; 
    @FXML private ProgressBar progressBar; 

    @FXML
    public void initialize() {
        if (Login.sesionUsuario != null) {
            int idUsuario = Login.sesionUsuario.getId();
            actualizarEstadisticas(idUsuario);
        } else {
            System.err.println("Error: No hay sesión de usuario activa.");
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

            // Actualizamos los textos
            cardsCount.setText(String.valueOf(cartasPoseidas));

            if (totalCartasApp > 0) {
                double porcentaje = (double) cartasPoseidas / totalCartasApp;
                
                if (progressPercent != null) {
                    progressPercent.setText(String.format("%.2f%%", porcentaje * 100));
                }
                if (progressBar != null) {
                    progressBar.setProgress(porcentaje);
                }
            }

        } catch (SQLException e) {
            Login.registrarEnLog("ERROR DASHBOARD STATS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}