package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ColeccionController {

    @FXML
    private GridPane cardGrid;

    // Usamos un Set para que la comprobación de "si la tengo" sea instantánea
    private Set<Integer> idsPoseidos = new HashSet<>();

   
@FXML
public void initialize() {
    new Thread(() -> {
        try {
            // 1. Cargamos de la BD (Trabajo pesado de red)
            cargarIdsPoseidos();
            
            // 2. Llamamos al método que las pinta de forma progresiva
            mostrarCartas();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

    private void cargarIdsPoseidos() {
        idsPoseidos.clear();
        // SQL para traer los IDs de la tabla de relación
        String sql = "SELECT id_carta FROM coleccion WHERE id_usuario = ?";
        
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Usamos el ID del usuario que guardamos al hacer Login
            pstmt.setInt(1, Login.sesionUsuario.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                idsPoseidos.add(rs.getInt("id_carta"));
            }
        } catch (SQLException e) {
            Login.registrarEnLog("ERROR CARGANDO POSEIDAS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarCartas() {
    // 1. PRIMERO: Limpiamos el grid en el hilo de la interfaz (FX Thread)
    javafx.application.Platform.runLater(() -> {
        cardGrid.getChildren().clear();
    });

    // 2. DESPUÉS: Lanzamos el hilo para procesar las 3130 cartas sin prisa
    new Thread(() -> {
        int column = 0;
        int row = 0;

        for (Carta carta : App.todasLasCartas) {
            boolean laTiene = idsPoseidos.contains(carta.getId_carta());
            
            // Creamos el objeto visual (esto se puede hacer en este hilo)
            VBox cardUI = createCard(carta, laTiene);

            // Variables finales para el runLater
            final int c = column;
            final int r = row;

            // 3. AÑADIR A LA PANTALLA: Esto DEBE ir en el hilo de FX
            javafx.application.Platform.runLater(() -> {
                cardGrid.add(cardUI, c, r);
            });

            // Incrementamos posición para la siguiente carta
            column++;
            if (column == 4) {
                column = 0;
                row++;
            }

            // Opcional: Pequeño respiro cada 50 cartas para que el PC no sufra
            if (column % 50 == 0) {
                try { Thread.sleep(2); } catch (InterruptedException e) {}
            }
        }
    }).start();
}

    private VBox createCard(Carta cardData, boolean poseida) {
    ImageView image = new ImageView();
        try {
            image.setImage(new Image(cardData.getImagen_url(), 100, 120, true, true));
        } catch (Exception e) {
            System.out.println("Error con imagen de: " + cardData.getNombre());
        }

        image.setFitWidth(100);
        image.setFitHeight(120);

        Label nameLabel = new Label(cardData.getNombre());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");
        nameLabel.setWrapText(true); // Ajusta el texto si el nombre es muy largo

        Label rarityLabel = new Label(cardData.getRareza());
        rarityLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");

        VBox card = new VBox(8);
        card.getChildren().addAll(image, nameLabel, rarityLabel);

        // APLICAMOS EL ESTILO SEGÚN SI LA TIENE O NO
        if (poseida) {
            card.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #f1c40f; 
                -fx-border-width: 2;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-padding: 10;
                -fx-alignment: center;
            """);
        } else {
            // Efecto "Desactivado": Bajamos opacidad y ponemos fondo gris
            card.setOpacity(0.35);
            card.setStyle("""
                -fx-background-color: #ecf0f1;
                -fx-border-color: #bdc3c7;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-padding: 10;
                -fx-alignment: center;
            """);
        }

        card.setPrefSize(140, 220);

        // Hacemos que el cursor cambie a una mano al pasar por encima
card.setCursor(javafx.scene.Cursor.HAND);

card.setOnMouseClicked(event -> {
    // 1. Guardamos en Supabase de forma asíncrona para que no de un tirón la App
    
        registrarCartaEnBD(cardData.getId_carta());
   

    // 2. Feedback visual inmediato en el hilo de la interfaz
    card.setOpacity(1.0); // La iluminamos
    card.setStyle("""
        -fx-background-color: white;
        -fx-border-color: #f1c40f; 
        -fx-border-width: 3;
        -fx-border-radius: 8;
        -fx-background-radius: 8;
        -fx-padding: 10;
        -fx-alignment: center;
    """);
    
    Login.registrarEnLog("Carta guardada en colección: " + cardData.getNombre());
});
        return card;
    }

    private void registrarCartaEnBD(int idCarta) {
    // 1. Verificamos si hay sesión (Si esto falla, el problema es el Login)
    if (Login.sesionUsuario == null) {
        System.err.println("ERROR: No hay sesión de usuario activa. No se puede guardar.");
        return;
    }

    int idUsuario = Login.sesionUsuario.getId();
    System.out.println("Intentando guardar: Usuario " + idUsuario + " -> Carta " + idCarta);

    String sql = "INSERT INTO coleccion (id_usuario, id_carta, cantidad) VALUES (?, ?, 1) " +
                 "ON CONFLICT (id_usuario, id_carta) DO NOTHING";

    try (Connection conn = Login.getConexion();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, idUsuario);
        pstmt.setInt(2, idCarta);
        
        int filasAfectadas = pstmt.executeUpdate();
        
        if (filasAfectadas > 0) {
            System.out.println("Guardado con éxito en la base de datos.");
        } else {
            System.out.println("La carta ya existía en la colección (no se insertó nada nuevo).");
        }
        
        // Es vital añadirla al Set local para que la UI sepa que ya la tienes
        idsPoseidos.add(idCarta);

    } catch (SQLException e) {
        System.err.println("ERROR SQL al guardar: " + e.getMessage());
        e.printStackTrace();
    }
}
}