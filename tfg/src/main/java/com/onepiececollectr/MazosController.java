package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MazosController {

    @FXML private GridPane deckGrid;
    @FXML private VBox deckList;
    @FXML private Label deckInfoLabel;
    @FXML private TextField deckNameField;
    @FXML private CheckBox redColor, blueColor, greenColor, yellowColor;

    private static List<Deck> misMazos = new ArrayList<>();
    public static Deck mazoSeleccionado = null;

    public static List<Deck> getMisMazos() { return misMazos; }

    @FXML
    public void initialize() {
        // Al entrar a la vista, si la lista está vacía, intentamos cargar
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

    /**
     * Carga todos los mazos del usuario desde Supabase
     */
    public static void cargarMazosDesdeBD() {
        String sql = "SELECT * FROM deck WHERE id_usuario = ?";
        Login loginManager = new Login();
        
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
                // Leemos la columna de colores que añadimos
                d.setColoresDesdeString(rs.getString("colores"));
                misMazos.add(d);
            }
            loginManager.registrarEnLog("Mazos sincronizados con éxito.");
        } catch (SQLException e) {
            loginManager.registrarEnLog("Error crítico cargando mazos: " + e.getMessage());
        }
    }

    /**
     * Crea un mazo nuevo en la base de datos y en la lista local
     */
    @FXML
    private void createDeck() {
        Login loginManager = new Login();
        String name = (deckNameField.getText() == null || deckNameField.getText().trim().isEmpty()) 
                      ? "Nuevo Mazo" : deckNameField.getText().trim();

        // 1. Recoger colores de la interfaz
        List<String> colors = new ArrayList<>();
        if (redColor.isSelected()) colors.add("#e74c3c");
        if (blueColor.isSelected()) colors.add("#3498db");
        if (greenColor.isSelected()) colors.add("#2ecc71");
        if (yellowColor.isSelected()) colors.add("#f1c40f");

        // 2. Insertar en base de datos
        String sql = "INSERT INTO deck (id_usuario, nombre, colores) VALUES (?, ?, ?)";
        
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setString(2, name);
            pstmt.setString(3, String.join(",", colors)); 
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                int idGenerado = rs.getInt(1);
                Deck newDeck = new Deck(idGenerado, Login.sesionUsuario.getId(), name);
                newDeck.setColores(colors);

                misMazos.add(newDeck);
                
                // Limpiar UI
                deckNameField.clear();
                redColor.setSelected(false);
                blueColor.setSelected(false);
                greenColor.setSelected(false);
                yellowColor.setSelected(false);
                
                refreshDeckList();
                loginManager.registrarEnLog("Mazo '" + name + "' guardado en la nube.");
            }
        } catch (SQLException e) {
            loginManager.registrarEnLog("Fallo al crear mazo: " + e.getMessage());
            loginManager.mostrarAlerta("Error", "No se pudo guardar el mazo. Revisa la conexión.");
        }
    }

    private void refreshDeckList() {
        if (deckList == null) return;
        deckList.getChildren().clear();
        for (Deck deck : misMazos) {
            Button btn = new Button(deck.getNombre_deck() + " " + getColorIcons(deck));
            btn.setPrefWidth(220);
            btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;", 
                         calculateGradient(deck)));
            btn.setOnAction(e -> openDeck(deck));
            deckList.getChildren().add(btn);
        }
    }

    private String calculateGradient(Deck deck) {
        if (deck.getColores().isEmpty()) return "#2c3e50";
        if (deck.getColores().size() == 1) return deck.getColores().get(0);
        return "linear-gradient(to right, " + String.join(", ", deck.getColores()) + ")";
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
        if (deckGrid == null) return;
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

    
}