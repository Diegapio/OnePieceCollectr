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


    private Set<Integer> idsPoseidos = new HashSet<>();

   
@FXML
public void initialize() {
    new Thread(() -> {
        try {
            
            cargarIdsPoseidos();
            
           
            mostrarCartas();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

    private void cargarIdsPoseidos() {
        idsPoseidos.clear();
        // SQL para tener los id de las cartas del usuario
        String sql = "SELECT id_carta FROM coleccion WHERE id_usuario = ?";
        
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            //Se guardan bajo el id del usuario que ha iniciado sesión
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
    //Vaciamos el grid en caso de que haya algo para evitar errores
    javafx.application.Platform.runLater(() -> {
        cardGrid.getChildren().clear();
    });

    // Lanzamos un hilo nuevo para procesar las cartas y hacerlas visibles sin ocupar todos los recursos de la app
    new Thread(() -> {
        int column = 0;
        int row = 0;

        for (Carta carta : App.todasLasCartas) {
            boolean laTiene = idsPoseidos.contains(carta.getId_carta());
            
            //Aprovechamos y creamos la interfaz de la carta
            VBox cardUI = createCard(carta, laTiene);

            final int c = column;
            final int r = row;

            //Añadimos a la interfaz el hilo para ir llenando la pantalla
            javafx.application.Platform.runLater(() -> {
                cardGrid.add(cardUI, c, r);
            });

            //Vamos sumando las columnas y filas para que tomen un espacio nuevo
            column++;
            if (column == 4) {
                column = 0;
                row++;
            }

            //Que cada 50 cartas se de un descanso para que no se congele
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

       //Si tenemos la carta, hacemos que cambie el tono y se vea más iluminada
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
            //Si no la tenemos, se ve como desactivada
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

        //Siempre viene bien que el ratón cambie a la mano para ayudar
card.setCursor(javafx.scene.Cursor.HAND);

card.setOnMouseClicked(event -> {
    //Al hacer click en una carta, se añade a la base de datos, najo el id del usuario
    
        registrarCartaEnBD(cardData.getId_carta());
   

    //Se ilumina nada más darle para que veamos que se ha hecho bien
    card.setOpacity(1.0); 
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
    //Vemos si se han guardado bien los datos del usuario, creo que es imposible pero hay que comprobar
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
        
        //Lo añadimos al hashset para que se vea en la interfaz
        idsPoseidos.add(idCarta);

    } catch (SQLException e) {
        System.err.println("ERROR SQL al guardar: " + e.getMessage());
        e.printStackTrace();
    }
}
}