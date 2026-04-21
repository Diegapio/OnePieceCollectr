package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import java.util.ArrayList;
import java.util.List;

public class MazosController {

    @FXML private GridPane deckGrid;
    @FXML private VBox deckList;
    @FXML private Label deckInfoLabel;
    @FXML private TextField deckNameField;
    @FXML private CheckBox redColor, blueColor, greenColor, yellowColor;

    // Lista temporal (lo ideal será cargarla de Supabase después)
    private static List<Deck> misMazos = new ArrayList<>();
    private static Deck mazoSeleccionado = null;

    @FXML
    public void initialize() {
        if (deckList != null) {
            refreshDeckList();
        }
        if (deckGrid != null && mazoSeleccionado != null) {
            renderDeck(mazoSeleccionado);
        }
    }

    private void refreshDeckList() {
        deckList.getChildren().clear();
        for (Deck deck : misMazos) {
            Button btn = new Button(deck.getNombre_deck() + " " + getColorIcons(deck));
            btn.setPrefWidth(220);
            
            // Estilo con degradado dinámico
            String style = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;", 
                                         calculateGradient(deck));
            btn.setStyle(style);
            btn.setOnAction(e -> openDeck(deck));
            deckList.getChildren().add(btn);
        }
    }

    private String calculateGradient(Deck deck) {
        if (deck.getColores().isEmpty()) return "#2c3e50";
        if (deck.getColores().size() == 1) return deck.getColores().get(0);
        
        return "linear-gradient(to right, " + String.join(", ", deck.getColores()) + ")";
    }

    @FXML
    private void createDeck() {
        String name = deckNameField.getText().isEmpty() ? "Nuevo Mazo" : deckNameField.getText();
        
        // Creamos el mazo (id_usuario de la sesión)
        Deck newDeck = new Deck(0, Login.sesionUsuario.getId(), name);

        List<String> colors = new ArrayList<>();
        if (redColor.isSelected()) colors.add("#e74c3c");
        if (blueColor.isSelected()) colors.add("#3498db");
        if (greenColor.isSelected()) colors.add("#2ecc71");
        if (yellowColor.isSelected()) colors.add("#f1c40f");
        newDeck.setColores(colors);

        misMazos.add(newDeck);
        deckNameField.clear();
        refreshDeckList();
    }

    public void renderDeck(Deck deck) {
        deckGrid.getChildren().clear();
        deckInfoLabel.setText(deck.getNombre_deck() + " (" + deck.getCartas().size() + "/50)");

        int col = 0, row = 0;
        for (Carta carta : deck.getCartas()) {
            VBox cardUI = createMiniCard(carta, deck);
            deckGrid.add(cardUI, col, row);
            if (++col == 4) { col = 0; row++; }
        }
    }

    private VBox createMiniCard(Carta carta, Deck deck) {
        ImageView img = new ImageView(new Image(carta.getImagen_url(), 80, 100, true, true));
        Button delBtn = new Button("X");
        delBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        delBtn.setOnAction(e -> {
            deck.getCartas().remove(carta);
            renderDeck(deck);
        });

        VBox box = new VBox(5, img, new Label(carta.getNombre()), delBtn);
        box.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-border-color: #ddd;");
        return box;
    }

    private void openDeck(Deck deck) {
        mazoSeleccionado = deck;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/deckDetail.fxml"));
            Parent view = loader.load();
            
            // Usamos tu clase Principal para cambiar la vista
            Principal.mostrarVista(view);
            
            MazosController controller = loader.getController();
            controller.renderDeck(deck);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getColorIcons(Deck deck) {
        StringBuilder icons = new StringBuilder();
        for (String c : deck.getColores()) {
            if (c.contains("e74c3c")) icons.append("🔴");
            if (c.contains("3498db")) icons.append("🔵");
            if (c.contains("2ecc71")) icons.append("🟢");
            if (c.contains("f1c40f")) icons.append("🟡");
        }
        return icons.toString();
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