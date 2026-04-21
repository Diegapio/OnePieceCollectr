package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;

public class CardDetailController {
    
    private Carta cartaActual;

    @FXML private ImageView cardImage;
    @FXML private Label nameLabel;
    @FXML private Label rarityLabel;
    @FXML private Label typeLabel; // Por si quieres añadir el tipo de carta

    // Este método lo llamarás desde el ColeccionController al hacer clic
    public void cargarDatos(Carta carta) {
        this.cartaActual = carta;

        if (carta.getImagen_url() != null) {
            // true, true para carga suave en segundo plano
            cardImage.setImage(new Image(carta.getImagen_url(), true));
        }

        nameLabel.setText(carta.getNombre());
        rarityLabel.setText("Rareza: " + carta.getRareza());
    }

    @FXML
    private void volver() {
        try {
            // Cargamos de nuevo la vista de colección
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/coleccion.fxml"));
            Parent root = loader.load();
            
            // Suponiendo que en tu clase Principal tienes acceso al panel donde cambias las vistas
            // Si usas un BorderPane central, sería algo así:
            Principal.mostrarVista(root); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
      @FXML
private void volverAlPrincipal(ActionEvent event) {
    try {
       //Botón para volver a atrás y estar en la vista principal para poder navegar guay guay
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
        Parent root = loader.load();
        
        
        Principal.mostrarVista(root);
        
    } catch (Exception e) {
        System.err.println("Error al volver al principal: " + e.getMessage());
        e.printStackTrace();
    }
}
}