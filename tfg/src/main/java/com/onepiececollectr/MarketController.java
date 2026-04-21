package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;

public class MarketController {

    @FXML private GridPane marketGrid;
    @FXML private Label statusLabel;
    @FXML private Label moneyLabel;

    // Lista de publicaciones (Clase Mercado que creamos antes)
    private List<Mercado> publicaciones = new ArrayList<>();

    @FXML
    public void initialize() {
        // En un TFG real, el dinero vendría de la tabla 'usuario'
        // Por ahora simulamos que el usuario tiene 50€
        moneyLabel.setText("💰 Tu Saldo: 50.00 €");

        // Aquí cargarías los datos de la base de datos. 
        // Simulamos una publicación para que veas cómo funciona:
        if (!App.todasLasCartas.isEmpty()) {
            Carta muestra = App.todasLasCartas.get(0); // Cogemos la primera carta de las 3130
            publicaciones.add(new Mercado(1, muestra, Login.sesionUsuario, 15.50, "Disponible"));
        }

        renderMarket();
    }

    private void renderMarket() {
        marketGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Mercado item : publicaciones) {
            VBox cardUI = createMarketCard(item);
            marketGrid.add(cardUI, column, row);

            column++;
            if (column == 4) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createMarketCard(Mercado item) {
        Carta c = item.getCarta();
        
        // Imagen desde URL (la de Supabase)
        ImageView image = new ImageView(new Image(c.getImagen_url(), 100, 120, true, true));
        
        Label name = new Label(c.getNombre());
        name.setStyle("-fx-font-weight: bold;");
        
        Label rarity = new Label(c.getRareza());
        Label price = new Label("💰 " + item.getPrecioFormateado());
        price.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14;");
        
        Label seller = new Label("👤 Vendedor: " + item.getVendedor().getNombre());
        seller.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d;");

        Button buyButton = new Button("Comprar");
        buyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");

        buyButton.setOnAction(e -> {
            // Lógica de compra simplificada
            statusLabel.setText("✅ Solicitud enviada por: " + c.getNombre());
            // Aquí iría la lógica de restar dinero y añadir a colección
        });

        VBox card = new VBox(8, image, name, rarity, price, seller, buyButton);
        card.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 10;
            -fx-alignment: center;
        """);

        return card;
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