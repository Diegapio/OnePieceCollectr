package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.awt.Desktop;
import java.net.URI;
import java.util.stream.Collectors;

public class MarketController {

    @FXML private GridPane marketGrid;
    @FXML private Label statusLabel;
    @FXML private Label moneyLabel;
    @FXML private TextArea txtIdsParaCardTrader;

    // Variables estáticas para que persistan al cambiar de vista
    public static boolean modoSeleccionMercado = false;
    public static List<Carta> listaParaOptimizar = new ArrayList<>();
    
    private List<Mercado> publicaciones = new ArrayList<>();

    @FXML
    public void initialize() {
        //moneyLabel.setText("💰 Tu Saldo: 50.00 €");

        /*  Simulación de datos
        if (!App.todasLasCartas.isEmpty()) {
            Carta muestra = App.todasLasCartas.get(0);
            publicaciones.add(new Mercado(1, muestra, Login.sesionUsuario, 15.50, "Disponible"));
        }

        //renderMarket();
        actualizarListaTexto();
    */
        }

   /*  private void renderMarket() {
        marketGrid.getChildren().clear();
        int column = 0, row = 0;

        for (Mercado item : publicaciones) {
            VBox cardUI = createMarketCard(item);
            marketGrid.add(cardUI, column, row);
            if (++column == 4) { column = 0; row++; }
        }
    }

    private VBox createMarketCard(Mercado item) {
        Carta c = item.getCarta();
        ImageView image = new ImageView(new Image(c.getImagen_url(), 100, 120, true, true));
        
        Label name = new Label(c.getNombre());
        name.setStyle("-fx-font-weight: bold;");
        
        Label price = new Label("💰 " + item.getPrecioFormateado());
        price.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14;");
        
        Button buyButton = new Button("Comprar");
        buyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        buyButton.setOnAction(e -> statusLabel.setText("✅ Solicitud enviada por: " + c.getNombre()));

        VBox card = new VBox(8, image, name, price, buyButton);
        card.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-padding: 10; -fx-alignment: center;");
        return card;
    }
        */

    @FXML
    private void irASeleccionarDeColeccion() {
        modoSeleccionMercado = true;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/coleccion.fxml"));
            Principal.mostrarVista(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void actualizarListaTexto() {
        if (txtIdsParaCardTrader != null) {
            String ids = listaParaOptimizar.stream()
                            .map(Carta::getId_carta)
                            .collect(Collectors.joining("\n"));
            txtIdsParaCardTrader.setText(ids);
        }
    }

    @FXML
    private void abrirCardTrader() {
        try {
            String url = "https://www.cardtrader.com/wishlists/new";
            Desktop.getDesktop().browse(new URI(url));
            statusLabel.setText("¡Copia los IDs y pégalos en CardTrader!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void limpiarLista() {
        listaParaOptimizar.clear();
        actualizarListaTexto();
        statusLabel.setText("Lista de optimización vaciada.");
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        modoSeleccionMercado = false; 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Principal.mostrarVista(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }
}