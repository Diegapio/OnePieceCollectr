package com.onepiececollectr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label cardsCount;
    @FXML private Label decksCount;
    @FXML private Label eventsCount;
    @FXML private Label progressPercent; // Nuevo: Para el texto "X.XX%"
    @FXML private ProgressBar progressBar; // Nuevo: Para la barra visual

    @FXML
    public void initialize() {
        if (Login.sesionUsuario != null) {
            // Ajusta a getId_usuario() o getId() según tu clase Usuario
            int idUsuario = Login.sesionUsuario.getId();
            actualizarEstadisticas(idUsuario);
        }
    }

    private void actualizarEstadisticas(int idUsuario) {
        int cartasPoseidas = 0;
        int totalCartasApp = App.todasLasCartas.size(); // Las 3130 cartas
        String sql = "SELECT COUNT(DISTINCT id_carta) FROM coleccion WHERE id_usuario = ?";

        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                cartasPoseidas = rs.getInt(1);
            }

            
            cardsCount.setText(String.valueOf(cartasPoseidas));
            
            if (totalCartasApp > 0) {
                double fraccion = (double) cartasPoseidas / totalCartasApp;
                
                
                if (progressPercent != null) {
                    progressPercent.setText(String.format("%.2f%%", fraccion * 100));
                }
                
                
                if (progressBar != null) {
                    progressBar.setProgress(fraccion);
                }
            }

            // Valores estáticos para rellenar la UI
            decksCount.setText("3");
            eventsCount.setText("2");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
public void volverAtras(ActionEvent event) {
    try {
        // Cargamos la vista principal (o la que quieras volver)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/principal.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        
        // Obtenemos el Stage actual a través de cualquier elemento (por ejemplo, cardsCount)
        Stage stage = (Stage) cardsCount.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }

 }
}