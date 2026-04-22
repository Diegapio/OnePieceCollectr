package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CardDetailController {
    
    private Carta cartaActual;

    @FXML private ImageView cardImage;
    @FXML private Label nameLabel;
    @FXML private Label rarityLabel;
    @FXML private Label typeLabel; 

    public void cargarDatos(Carta carta) {
        this.cartaActual = carta;
        if (carta.getImagen_url() != null) {
            cardImage.setImage(new Image(carta.getImagen_url(), true));
        }
        nameLabel.setText(carta.getNombre());
        rarityLabel.setText("Rareza: " + carta.getRareza());
        if (typeLabel != null) {
            typeLabel.setText("Tipo: " + carta.getTipo());
        }
    }

    @FXML
private void añadirAlMazoActual() {
    Login loginManager = new Login();
    
    // Accedemos al mazo que marcamos como seleccionado en MazosController
    Deck mazo = MazosController.mazoSeleccionado;
    
    if (mazo == null) {
        loginManager.mostrarAlerta("Sin mazo", "Selecciona primero un mazo en la pestaña de Mazos.");
        return;
    }

    // --- EL FILTRO CORREGIDO ---
    // Comparamos el ID de la carta que queremos añadir con los IDs de las que ya están en el mazo
    long copias = mazo.getCartas().stream()
            .filter(c -> String.valueOf(c.getId_carta()).equals(String.valueOf(cartaActual.getId_carta())))
            .count();

    if (copias >= 4) {
        loginManager.mostrarAlerta("Límite de copias", "Ya tienes 4 copias de esta carta en el mazo.");
        return;
    }

    // --- REGLA DEL LÍDER ---
    // Usamos el método getTipo() de tu clase Carta
    if (cartaActual.getTipo() != null && cartaActual.getTipo().equalsIgnoreCase("LIDER")) {
        boolean tieneLider = mazo.getCartas().stream()
                .anyMatch(c -> c.getTipo() != null && c.getTipo().equalsIgnoreCase("LIDER"));
        
        if (tieneLider) {
            loginManager.mostrarAlerta("Regla de Líder", "Este mazo ya tiene un Líder.");
            return;
        }
    }

    // --- GUARDAR EN LA TABLA deck_carta ---
    String sql = "INSERT INTO deck_carta (id_deck, id_carta, cantidad) VALUES (?, ?, 1) " +
                 "ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = deck_carta.cantidad + 1";

    try (Connection conn = Login.getConexion();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, mazo.getId_deck());
        // Enviamos el ID de la carta (asegúrate de si es int o String en tu objeto Carta)
        pstmt.setObject(2, cartaActual.getId_carta()); 
        
        pstmt.executeUpdate();

        // Añadimos la carta a la lista del mazo en memoria
        mazo.getCartas().add(cartaActual);
        
        loginManager.mostrarAlerta("Éxito", "Carta añadida al mazo correctamente.");
        loginManager.registrarEnLog("Añadida carta " + cartaActual.getNombre() + " al mazo ID: " + mazo.getId_deck());

    } catch (SQLException e) {
        loginManager.registrarEnLog("Error SQL: " + e.getMessage());
        System.err.println("Error al insertar en deck_carta: " + e.getMessage());
    }
}

    @FXML
    private void volver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/coleccion.fxml"));
            Parent root = loader.load();
            Principal.mostrarVista(root); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Parent root = loader.load();
            Principal.mostrarVista(root);
        } catch (Exception e) {
            System.err.println("Error al volver al principal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}