package com.onepiececollectr;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MazosController {

    @FXML private GridPane deckGrid;
    @FXML private VBox deckList;
    @FXML private Label deckInfoLabel;
    @FXML private TextField deckNameField;

    @FXML private CheckBox redColor;
    @FXML private CheckBox blueColor;
    @FXML private CheckBox greenColor;
    @FXML private CheckBox yellowColor;

    // 🔥 NUEVOS COLORES
    @FXML private CheckBox purpleColor;
    @FXML private CheckBox blackColor;

    private static List<Deck> misMazos = new ArrayList<>();
    public static Deck mazoSeleccionado = null;

    Login login = new Login();

    @FXML private Button btnMazoIA;

    public static List<Deck> getMisMazos() {
        return misMazos;
    }

    @FXML
    public void initialize() {

        if (misMazos.isEmpty() && Login.sesionUsuario != null) {
            cargarMazosDesdeBD();
        }

        if (deckList != null) {
            refreshDeckList();
        }

        if (deckGrid != null && mazoSeleccionado != null) {
            renderDeck(mazoSeleccionado);
        }
    }

    public static void cargarMazosDesdeBD() {
        String sql = "SELECT * FROM deck WHERE id_usuario = ?";

        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Login.sesionUsuario.getId());
            ResultSet rs = pstmt.executeQuery();

            misMazos.clear();

            while (rs.next()) {
                Deck d = new Deck(
                        rs.getInt("id_deck"),
                        rs.getInt("id_usuario"),
                        rs.getString("nombre")
                );
                d.setColoresDesdeString(rs.getString("colores"));
                misMazos.add(d);
            }

        } catch (SQLException e) {
            Login.registrarEnLog("Error cargando mazos: " + e.getMessage());
        }
    }

    @FXML
    private void createDeck() {

        String name = (deckNameField.getText() != null && !deckNameField.getText().trim().isEmpty())
                ? deckNameField.getText().trim()
                : "Nuevo Mazo";

        List<String> colors = new ArrayList<>();

        if (redColor.isSelected()) colors.add("#e74c3c");
        if (blueColor.isSelected()) colors.add("#3498db");
        if (greenColor.isSelected()) colors.add("#2ecc71");
        if (yellowColor.isSelected()) colors.add("#f1c40f");

        // 🔥 NUEVOS
        if (purpleColor != null && purpleColor.isSelected()) colors.add("#9b59b6");
        if (blackColor != null && blackColor.isSelected()) colors.add("#2c3e50");

        String sql = "INSERT INTO deck (id_usuario, nombre, colores) VALUES (?, ?, ?)";

        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setString(2, name);
            pstmt.setString(3, String.join(",", colors));
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                int id = rs.getInt(1);

                Deck newDeck = new Deck(id, Login.sesionUsuario.getId(), name);
                newDeck.setColores(colors);

                misMazos.add(newDeck);
                refreshDeckList();

                deckNameField.clear();
                redColor.setSelected(false);
                blueColor.setSelected(false);
                greenColor.setSelected(false);
                yellowColor.setSelected(false);

                if (purpleColor != null) purpleColor.setSelected(false);
                if (blackColor != null) blackColor.setSelected(false);
            }

        } catch (SQLException e) {
            login.mostrarAlerta("Error", "No se pudo crear el mazo");
        }
    }

    private void refreshDeckList() {

        deckList.getChildren().clear();

        for (Deck deck : misMazos) {

            HBox fila = new HBox(10);
            fila.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5;");

            Button btn = new Button(
                    deck.getNombre_deck() + " " + getColorIcons(deck)
            );

            btn.setPrefWidth(220);
            btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 10;
                -fx-background-radius: 8;
            """, calculateGradient(deck)));

            btn.setOnAction(e -> openDeck(deck));

            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> borrarMazo(deck));

            fila.getChildren().addAll(btn, deleteBtn);
            deckList.getChildren().add(fila);
        }
    }

    private String calculateGradient(Deck deck) {

        if (deck.getColores().isEmpty()) {
            return "#2c3e50";
        }

        if (deck.getColores().size() == 1) {
            return deck.getColores().get(0);
        }

        return "linear-gradient(to right, " + String.join(", ", deck.getColores()) + ")";
    }

    private String getColorIcons(Deck deck) {

        StringBuilder icons = new StringBuilder();

        for (String c : deck.getColores()) {

            if (c.equals("#e74c3c")) icons.append("🔴");
            if (c.equals("#3498db")) icons.append("🔵");
            if (c.equals("#2ecc71")) icons.append("🟢");
            if (c.equals("#f1c40f")) icons.append("🟡");
            if (c.equals("#9b59b6")) icons.append("🟣"); // nuevo
            if (c.equals("#2c3e50")) icons.append("⚫"); // nuevo
        }

        return icons.toString();
    }

    private void openDeck(Deck deck) {

        mazoSeleccionado = deck;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/deckDetail.fxml"));
            Parent view = loader.load();

            Principal.mostrarVista(view);

            MazosController controller = loader.getController();
            controller.renderDeck(deck);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderDeck(Deck deck) {

        deckGrid.getChildren().clear();

        deckInfoLabel.setText(
                deck.getNombre_deck() + " (" + deck.getCartas().size() + "/50)"
        );

        int col = 0;
        int row = 0;

        for (Carta c : deck.getCartas()) {

            VBox card = createMiniCard(c, deck);

            deckGrid.add(card, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createMiniCard(Carta carta, Deck deck) {

        ImageView img = new ImageView(
                new Image(carta.getImagen_url(), 80, 100, true, true)
        );

        Button del = new Button("X");

        del.setOnAction(e -> {
            deck.getCartas().remove(carta);
            renderDeck(deck);
        });

        VBox box = new VBox(5, img, new Label(carta.getNombre()), del);
        box.setStyle("-fx-alignment: center;");

        return box;
    }

    private void borrarMazo(Deck mazo) {

        String sql = "DELETE FROM deck WHERE id_deck = ?";

        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mazo.getId_deck());
            pstmt.executeUpdate();

            misMazos.remove(mazo);
            refreshDeckList();

        } catch (SQLException e) {
            login.mostrarAlerta("Error", "No se pudo borrar el mazo");
        }
    }

    @FXML
    private void irAGeneradorIA() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/generadorIA.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnMazoIA.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
private void exportarMazoPDF(ActionEvent event) {

    if (mazoSeleccionado == null || mazoSeleccionado.getCartas().isEmpty()) {
        login.mostrarAlerta("Error", "El mazo está vacío o no seleccionado.");
        return;
    }

    Map<String, Integer> conteo = new HashMap<>();

    for (Carta c : mazoSeleccionado.getCartas()) {
        conteo.put(c.getNombre(), conteo.getOrDefault(c.getNombre(), 0) + 1);
    }

    StringBuilder contenido = new StringBuilder();
    contenido.append("<h1>").append(mazoSeleccionado.getNombre_deck()).append("</h1><ul>");

    conteo.forEach((nombre, cantidad) ->
            contenido.append("<li>").append(cantidad).append("x ").append(nombre).append("</li>")
    );

    contenido.append("</ul>");

    generarDocumentoPDF(mazoSeleccionado.getNombre_deck(), contenido.toString());
}
private void generarDocumentoPDF(String nombre, String html) {

    try {
        File file = new File("mazo_" + nombre + ".html");
        FileWriter writer = new FileWriter(file);

        writer.write("<html><body>" + html + "</body></html>");
        writer.close();

        Desktop.getDesktop().browse(file.toURI());

    } catch (Exception e) {
        e.printStackTrace();
    }
}

}