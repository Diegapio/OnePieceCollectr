package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CardDetailController {

    @FXML private ImageView cardImage;
    @FXML private Label nameLabel;
    @FXML private Label lblCantidad; // El que dice "x0", "x1"...
    @FXML private Button btnMas;
    @FXML private Button btnMenos;

    private Carta cartaActual;
    private int cantidadEnMazo = 0;

    public void cargarDatos(Carta carta) {
        this.cartaActual = carta;
        nameLabel.setText(carta.getNombre());
        if (carta.getImagen_url() != null) {
            cardImage.setImage(new Image(carta.getImagen_url(), true));
        }

        // Al abrir la pantalla, miramos cuántas copias hay ya en el mazo seleccionado
        this.cantidadEnMazo = obtenerCantidadEnMazo(carta.getId_carta());
        actualizarInterfaz();
    }

    @FXML
    private void handleMas() {
        // Regla: Si es LIDER máximo 1, si no máximo 4
        int limite = (cartaActual.getTipo() != null && cartaActual.getTipo().equalsIgnoreCase("LIDER")) ? 1 : 4;
        
        if (cantidadEnMazo < limite) {
            cantidadEnMazo++;
            guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
            actualizarInterfaz();
        }
    }

    @FXML
    private void handleMenos() {
        if (cantidadEnMazo > 0) {
            cantidadEnMazo--;
            guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
            actualizarInterfaz();
        }
    }

    // NUEVO: Método para poner el máximo de copias permitido (x4 o x1 si es líder)
    @FXML
    private void handleMas4() {
        int limite = (cartaActual.getTipo() != null && cartaActual.getTipo().equalsIgnoreCase("LIDER")) ? 1 : 4;
        cantidadEnMazo = limite;
        guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
        actualizarInterfaz();
    }

    // NUEVO: Método para quitar todas las copias (x0)
    @FXML
    private void handleMenos4() {
        cantidadEnMazo = 0;
        guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
        actualizarInterfaz();
    }

    private void actualizarInterfaz() {
        lblCantidad.setText("x" + cantidadEnMazo);
        
        // Límite visual para el feedback de color
        int limite = (cartaActual.getTipo() != null && cartaActual.getTipo().equalsIgnoreCase("LIDER")) ? 1 : 4;

        if (cantidadEnMazo >= limite) {
            lblCantidad.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 20;");
            btnMas.setDisable(true);
        } else {
            lblCantidad.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 20;");
            btnMas.setDisable(false);
        }
    }

    private int obtenerCantidadEnMazo(String idCarta) {
        if (MazosController.mazoSeleccionado == null) return 0;

        String sql = "SELECT cantidad FROM deck_carta WHERE id_deck = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, MazosController.mazoSeleccionado.getId_deck());
            pstmt.setString(2, idCarta);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("cantidad");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void guardarEnBD(String idCarta, int nuevaCantidad) {
        if (MazosController.mazoSeleccionado == null) return;

        if (nuevaCantidad == 0) {
            String sql = "DELETE FROM deck_carta WHERE id_deck = ? AND id_carta = ?";
            ejecutarUpdate(sql, MazosController.mazoSeleccionado.getId_deck(), idCarta, -1);
        } else {
            String sql = "INSERT INTO deck_carta (id_deck, id_carta, cantidad) VALUES (?, ?, ?) " +
                         "ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = EXCLUDED.cantidad";
            ejecutarUpdate(sql, MazosController.mazoSeleccionado.getId_deck(), idCarta, nuevaCantidad);
        }
    }

    private void ejecutarUpdate(String sql, int idMazo, String idCarta, int cant) {
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMazo);
            pstmt.setString(2, idCarta);
            if (cant != -1) pstmt.setInt(3, cant);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
private void handleCerrar() {
    // Cierra la ventanita actual
    Stage stage = (Stage) lblCantidad.getScene().getWindow();
    stage.close();
}
}