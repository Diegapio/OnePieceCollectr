package com.onepiececollectr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class DashboardController {

    @FXML public  Label       cardsCount;
    @FXML private Label       decksCount;
    @FXML private Label       eventsCount;
    @FXML private Label       progressPercent;
    @FXML private ProgressBar progressBar;
    @FXML private Label       nextEventLabel;

    public static DashboardController instancia;

    @FXML
    public void initialize() {
        instancia = this;
        if (Login.sesionUsuario != null) {
            refrescar();
        }
    }

    public void refrescar() {
        int idUsuario = Login.sesionUsuario.getId();
        int totalCartasApp = App.todasLasCartas.size();

        new Thread(() -> {
            int cartasPoseidas = 0;
            int numMazos = 0;
            int numEventos = 0;
            String nombreProximo = null;
            String fechaProximo = null;

            try (Connection conn = Login.getConexion()) {

                // 1. Cartas poseídas
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(DISTINCT id_carta) FROM coleccion WHERE id_usuario = ?")) {
                    ps.setInt(1, idUsuario);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) cartasPoseidas = rs.getInt(1);
                }

                // 2. Mazos
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM deck WHERE id_usuario = ?")) {
                    ps.setInt(1, idUsuario);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) numMazos = rs.getInt(1);
                }

                // 3. Eventos futuros (hoy inclusive)
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM eventos WHERE id_usuario = ? " +
                        "AND TO_DATE(fecha, 'DD/MM/YYYY') >= CURRENT_DATE")) {
                    ps.setInt(1, idUsuario);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) numEventos = rs.getInt(1);
                }

                // 4. Próximo evento
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT nombre, fecha FROM eventos WHERE id_usuario = ? " +
                        "AND TO_DATE(fecha, 'DD/MM/YYYY') >= CURRENT_DATE " +
                        "ORDER BY TO_DATE(fecha, 'DD/MM/YYYY') ASC LIMIT 1")) {
                    ps.setInt(1, idUsuario);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        nombreProximo = rs.getString("nombre");
                        fechaProximo  = rs.getString("fecha");
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Login.registrarEnLog("ERROR DASHBOARD: " + e.getMessage());
            }

            final int    cp       = cartasPoseidas;
            final int    mazos    = numMazos;
            final int    eventos  = numEventos;
            final double frac     = totalCartasApp > 0 ? (double) cp / totalCartasApp : 0.0;
            final String pctTxt   = String.format("%.2f%%", frac * 100);
            final String proxNom  = nombreProximo;
            final String proxFech = fechaProximo;

            Platform.runLater(() -> {
                if (cardsCount      != null) cardsCount.setText(String.valueOf(cp));
                if (decksCount      != null) decksCount.setText(String.valueOf(mazos));
                if (eventsCount     != null) eventsCount.setText(String.valueOf(eventos));
                if (progressPercent != null) progressPercent.setText(pctTxt);
                if (progressBar     != null) progressBar.setProgress(frac);

                if (nextEventLabel != null) {
                    if (proxNom != null) {
                        nextEventLabel.setText(proxNom + " [" + proxFech + "]");
                        nextEventLabel.setStyle("-fx-font-size:16;-fx-text-fill:#c8dce8;-fx-font-weight:bold;");
                    } else {
                        nextEventLabel.setText("Sin eventos programados");
                        nextEventLabel.setStyle("-fx-font-size:14;-fx-text-fill:#4a6fa5;");
                    }
                }
            });

        }, "dashboard-refresh").start();
    }

    @FXML private void onCardHover() {}
    @FXML private void onCardExit()  {}
}
