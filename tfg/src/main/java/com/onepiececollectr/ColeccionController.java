package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.onepiececollectr.models.Card;
import java.util.ArrayList;
import java.util.List;

public class ColeccionController {
private List<Card> cards = new ArrayList<>();
    @FXML
    private GridPane cardGrid;
@FXML
public void initialize() {

    cards.add(new Card("Monkey D. Luffy", "SR", "/cards/luffy.jpg"));
    cards.add(new Card("Zoro", "R", "/cards/zoro.jpg"));
    cards.add(new Card("Nami", "UC", "/cards/nami.jpg"));
    cards.add(new Card("Sanji", "SR", "/cards/sanji.jpg"));

    int column = 0;
    int row = 0;

    for (Card card : cards) {

        VBox cardUI = createCard(card);

        cardGrid.add(cardUI, column, row);

        column++;

        if (column == 4) {
            column = 0;
            row++;
        }
    }
}

private VBox createCard(Card cardData) {

    var url = getClass().getResource(cardData.getImage());

if (url == null) {
    System.out.println("Imagen no encontrada: " + cardData.getImage());
}

ImageView image = new ImageView(new Image(url.toExternalForm()));

    image.setFitWidth(100);
    image.setFitHeight(120);

    Label nameLabel = new Label(cardData.getName());
    nameLabel.setStyle("-fx-font-weight: bold;");

    Label rarityLabel = new Label(cardData.getRarity());

    VBox card = new VBox(8);
    card.getChildren().addAll(image, nameLabel, rarityLabel);

    card.setStyle("""
        -fx-background-color: white;
        -fx-border-color: black;
        -fx-border-radius: 8;
        -fx-background-radius: 8;
        -fx-padding: 10;
        -fx-alignment: center;
    """);

    card.setPrefSize(140,220);

    return card;
}
}