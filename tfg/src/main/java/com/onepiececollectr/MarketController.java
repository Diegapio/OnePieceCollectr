package com.onepiececollectr;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.awt.Desktop;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MarketController {

    @FXML private Label statusLabel;
    @FXML private Label moneyLabel;
    @FXML private TextArea txtIdsParaCardTrader;

    public static boolean modoSeleccionMercado = false;

    public static List<Carta> listaParaOptimizar = new ArrayList<>();

    public static Set<String> idsPoseidos = new HashSet<>();

    public static MarketController instancia;

    @FXML
    public void initialize() {

        instancia = this;

        actualizarListaTexto();

        new Thread(() -> {
            cargarIdsPoseidos();

            Platform.runLater(() -> statusLabel.setText(
                    listaParaOptimizar.isEmpty()
                            ? "Selecciona cartas de tu colección para optimizar."
                            : listaParaOptimizar.size() + " carta(s) seleccionada(s)."
            ));
        }).start();
    }

    @FXML
    private void irASeleccionarDeColeccion() {

        modoSeleccionMercado = true;

        Login.registrarEnLog("MERCADO: Usuario entró a seleccionar cartas.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/coleccion.fxml"));
            Principal.mostrarVista(loader.load());

        } catch (Exception e) {
            e.printStackTrace();

            Login.registrarEnLog(
                    "ERROR MERCADO: No se pudo cargar coleccion.fxml - "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void abrirCardTrader() {

        if (listaParaOptimizar.isEmpty()) {
            statusLabel.setText("Añade cartas primero.");
            return;
        }

        try {

            Desktop.getDesktop().browse(
                    new URI("https://www.cardtrader.com/wishlists/new")
            );

            statusLabel.setText("¡Copia los IDs y pégalos en CardTrader!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void limpiarLista() {

        listaParaOptimizar.clear();

        actualizarListaTexto();

        statusLabel.setText("Lista vaciada.");
    }

    @FXML
    private void volverAlPrincipal() {

        modoSeleccionMercado = false;

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));

            Principal.mostrarVista(loader.load());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void actualizarListaTexto() {

    if (instancia == null) return;

    if (instancia.txtIdsParaCardTrader == null) return;

    StringBuilder sb = new StringBuilder();

    List<Carta> poseidas = listaParaOptimizar.stream()
            .filter(c -> idsPoseidos.contains(c.getId_carta()))
            .toList();

    List<Carta> noPoseidas = listaParaOptimizar.stream()
            .filter(c -> !idsPoseidos.contains(c.getId_carta()))
            .toList();

    // ─────────────────────────────
    // POSEÍDAS
    // ─────────────────────────────

    sb.append("===== IDS POSEÍDOS =====\n\n");

    if (poseidas.isEmpty()) {

        sb.append("Ninguna carta poseída seleccionada.\n");

    } else {

        for (Carta c : poseidas) {

            sb.append(c.getId_carta())
                    .append(" - ")
                    .append(c.getNombre())
                    .append("\n");
        }
    }

    // ─────────────────────────────
    // NO POSEÍDAS
    // ─────────────────────────────

    sb.append("\n\n===== IDS NO POSEÍDOS =====\n\n");

    if (noPoseidas.isEmpty()) {

        sb.append("Ninguna carta no poseída seleccionada.\n");

    } else {

        for (Carta c : noPoseidas) {

            sb.append(c.getId_carta())
                    .append(" - ")
                    .append(c.getNombre())
                    .append("\n");
        }
    }

    instancia.txtIdsParaCardTrader.setText(sb.toString());

    instancia.statusLabel.setText(
            listaParaOptimizar.size()
                    + " carta(s) seleccionada(s)."
    );
}

    private void cargarIdsPoseidos() {

        idsPoseidos.clear();

        String sql = "SELECT id_carta FROM coleccion WHERE id_usuario = ?";

        try (
                Connection conn = Login.getConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, Login.sesionUsuario.getId());

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                idsPoseidos.add(rs.getString("id_carta"));
            }

        } catch (SQLException e) {

            Login.registrarEnLog(
                    "ERROR MERCADO: " + e.getMessage()
            );
        }
    }
}