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

    // instancia estática para que Principal pueda refrescar sin guardar una referencia manual
    public static DashboardController instancia;

    @FXML
    public void initialize() {
        instancia = this;
        if (Login.sesionUsuario != null) {
            refrescar();
        }
    }

    /**
     * Carga los datos en un hilo de fondo y actualiza la UI en el hilo JavaFX.
     * Se puede llamar desde Principal cada vez que el usuario navega al dashboard.
     */
    public void refrescar() {
        int idUsuario = Login.sesionUsuario.getId();

        new Thread(() -> {
            // ── Cartas poseídas (BD) ───────────────────────────────────────
            int cartasPoseidas = 0;
            int totalCartasApp = App.todasLasCartas.size();

            String sql = "SELECT COUNT(DISTINCT id_carta) FROM coleccion WHERE id_usuario = ?";
            try (Connection conn = Login.getConexion()) {

                // Verificar que la conexión sigue activa
                if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                    Login.registrarEnLog("Conexión inválida al cargar dashboard");
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, idUsuario);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) cartasPoseidas = rs.getInt(1);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Login.registrarEnLog("ERROR DASHBOARD: " + e.getMessage());
            }

            // ── Mazos y eventos (en memoria, rápidos) ──────────────────────
            int numMazos   = MazosController.getMisMazos().size();
            int numEventos = EventosController.getListaEventos().size();

            // Variables finales para el lambda
            final int    cp     = cartasPoseidas;
            final double frac   = totalCartasApp > 0 ? (double) cp / totalCartasApp : 0.0;
            final String pctTxt = String.format("%.2f%%", frac * 100);
            final Event  proximo = numEventos > 0 ? EventosController.getListaEventos().get(0) : null;

            // ── Actualizar UI en el hilo JavaFX ────────────────────────────
            Platform.runLater(() -> {
                if (cardsCount     != null) cardsCount.setText(String.valueOf(cp));
                if (decksCount     != null) decksCount.setText(String.valueOf(numMazos));
                if (eventsCount    != null) eventsCount.setText(String.valueOf(numEventos));
                if (progressPercent!= null) progressPercent.setText(pctTxt);
                if (progressBar    != null) progressBar.setProgress(frac);

                if (nextEventLabel != null) {
                    if (proximo != null) {
                        nextEventLabel.setText(proximo.getName() + " [" + proximo.getDate() + "]");
                        nextEventLabel.setStyle("-fx-font-size:16;-fx-text-fill:#2c3e50;-fx-font-weight:bold;");
                    } else {
                        nextEventLabel.setText("Sin eventos programados");
                        nextEventLabel.setStyle("-fx-font-size:14;-fx-text-fill:#666;");
                    }
                }
            });

        }, "dashboard-refresh").start();
    }

    // ── Métodos de hover (FXML, se mantienen vacíos o se eliminan del FXML) ──
    @FXML private void onCardHover() {}
    @FXML private void onCardExit()  {}
}