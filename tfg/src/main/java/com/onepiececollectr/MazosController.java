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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MazosController {

    // Cosas FXML
    @FXML private GridPane deckGrid;
    @FXML private VBox deckList;
    @FXML private Label deckInfoLabel;
    @FXML private TextField deckNameField;
    @FXML private Label lblContador; 

    @FXML private CheckBox redColor, blueColor, greenColor, yellowColor, purpleColor, blackColor;
    @FXML private Button btnMazoIA;
    @FXML private void sumarUno() { cambiarCantidad(1); }
    @FXML private void restarUno() { cambiarCantidad(-1); }
    @FXML private void sumarMax() { cambiarCantidad(4); }
    @FXML private void restarMax() { cambiarCantidad(-4); }

    // Variables de clase
    private Carta cartaActual;      
    private int cantidadEnMazo = 0;
    private static List<Deck> misMazos = new ArrayList<>();
    public static Deck mazoSeleccionado = null;
    Login login = new Login();

    public static List<Deck> getMisMazos() { return misMazos; }

    @FXML
    public void initialize() {
        if (misMazos.isEmpty() && Login.sesionUsuario != null) {
            cargarMazosDesdeBD();
        }

        if (deckList != null) {
            refreshDeckList();
        }

        if (deckGrid != null && mazoSeleccionado != null) {
            cargarCartasDelMazo(mazoSeleccionado);
            renderDeck(mazoSeleccionado);
        }
    }

    // Gestión de mazos, crear, borrar, cargar desde BD, etc.

    public static void cargarMazosDesdeBD() {
        String sql = "SELECT * FROM deck WHERE id_usuario = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            ResultSet rs = pstmt.executeQuery();
            misMazos.clear();
            while (rs.next()) {
                Deck d = new Deck(rs.getInt("id_deck"), rs.getInt("id_usuario"), rs.getString("nombre"));
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
                ? deckNameField.getText().trim() : "Nuevo Mazo";

        List<String> colors = new ArrayList<>();
        if (redColor.isSelected()) colors.add("#e74c3c");
        if (blueColor.isSelected()) colors.add("#3498db");
        if (greenColor.isSelected()) colors.add("#2ecc71");
        if (yellowColor.isSelected()) colors.add("#f1c40f");
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
                Deck newDeck = new Deck(rs.getInt(1), Login.sesionUsuario.getId(), name);
                newDeck.setColores(colors);
                misMazos.add(newDeck);
                refreshDeckList();
                limpiarCamposCreacion();
            }
        } catch (SQLException e) { login.mostrarAlerta("Error", "No se pudo crear el mazo"); }
    }

    private void refreshDeckList() {
        if (deckList == null) return;
        deckList.getChildren().clear();
        for (Deck deck : misMazos) {
            HBox fila = new HBox(10);
            fila.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5;");
            Button btn = new Button(deck.getNombre_deck() + " " + getColorIcons(deck));
            btn.setPrefWidth(220);
            btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;", calculateGradient(deck)));
            btn.setOnAction(e -> openDeck(deck));
            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> borrarMazo(deck));
            fila.getChildren().addAll(btn, deleteBtn);
            deckList.getChildren().add(fila);
        }
    }

    private void borrarMazo(Deck mazo) {
        String sql = "DELETE FROM deck WHERE id_deck = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mazo.getId_deck());
            pstmt.executeUpdate();
            misMazos.remove(mazo);
            refreshDeckList();
        } catch (SQLException e) { login.mostrarAlerta("Error", "No se pudo borrar"); }
    }

    // Gestión de cartas dentro del mazo, cargar cartas, renderizar, abrir detalle, contador, etc.

 public void cargarCartasDelMazo(Deck mazo) {
    mazo.getCartas().clear();

    String sql = "SELECT c.*, dc.cantidad FROM carta c " +
                 "JOIN deck_carta dc ON c.id_carta = dc.id_carta " +
                 "WHERE dc.id_deck = ?";
    
    try (Connection conn = Login.getConexion();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, mazo.getId_deck());
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            int cantidadEnBD = rs.getInt("cantidad");  
            
            for (int i = 0; i < cantidadEnBD; i++) {
                mazo.getCartas().add(new Carta(
                rs.getString("id_carta"),
                rs.getString("nombre"),
                rs.getString("tipo"),
                rs.getString("color"),
                rs.getString("rareza"),
                rs.getString("imagen_url"),
                rs.getString("texto"),
                (Integer) rs.getObject("coste"),
                (Integer) rs.getObject("poder"),
                (Integer) rs.getObject("contador"),
                (String) rs.getString("subtipos"),
                (String) rs.getString("atributo")
            ));
            }
        }
    } catch (SQLException e) { e.printStackTrace(); }
}

public void renderDeck(Deck mazo) {
    if (deckGrid == null || mazo == null) return;
    deckGrid.getChildren().clear();

   
    Map<String, Integer> conteo = new HashMap<>();
    Map<String, Carta> unicas = new HashMap<>();

    for (Carta c : mazo.getCartas()) {
        conteo.put(c.getId_carta(), conteo.getOrDefault(c.getId_carta(), 0) + 1);
        unicas.put(c.getId_carta(), c);
    }

    
    int col = 0, row = 0;
    for (String id : conteo.keySet()) {
        Carta carta = unicas.get(id);
        int cantidad = conteo.get(id); // <--- Aquí ya traerá el 4

        VBox cardVisual = createMiniCardConMultiplicador(carta, mazo, cantidad);
        deckGrid.add(cardVisual, col, row);

        if (++col == 4) { col = 0; row++; }
    }
    
    deckInfoLabel.setText(mazo.getNombre_deck() + " (" + mazo.getCartas().size() + "/50)");
}

   private VBox createMiniCardConMultiplicador(Carta carta, Deck deck, int cantidad) {
    StackPane stack = new StackPane();
    ImageView img = new ImageView(new Image(carta.getImagen_url(), 80, 110, true, true));
    
    
    img.setOnMouseClicked(e -> { 
        this.cartaActual = carta; 
        this.cantidadEnMazo = cantidad; 
        actualizarVista(); 
        abrirVentanaDetalle(carta);
    });

    Label lbl = new Label("x" + cantidad);
    lbl.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 5;");
    StackPane.setAlignment(lbl, javafx.geometry.Pos.BOTTOM_RIGHT);
    stack.getChildren().addAll(img, lbl);

    Button del = new Button("X");
    del.setOnAction(e -> { borrarFilaCarta(deck, carta); cargarCartasDelMazo(deck); renderDeck(deck); });

    return new VBox(5, stack, new Label(carta.getNombre()), del);
}

private void abrirVentanaDetalle(Carta carta) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cardPopup.fxml"));
        Parent root = loader.load();
        
        CardDetailController controller = loader.getController();
        controller.cargarDatos(carta);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));

        
        stage.setOnHiding(event -> {
            cargarCartasDelMazo(mazoSeleccionado); 
            renderDeck(mazoSeleccionado);
        });

        stage.show();
    } catch (IOException e) { e.printStackTrace(); }
}

    private void borrarFilaCarta(Deck deck, Carta carta) {
        String sql = "DELETE FROM deck_carta WHERE id_deck = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, deck.getId_deck()); pstmt.setString(2, carta.getId_carta()); pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Ultimos cambios, contador y botones para sumar/restar cartas al mazo, con actualización en BD y vista

    

    private void cambiarCantidad(int delta) {
        if (mazoSeleccionado == null || cartaActual == null) return;
        int max = cartaActual.getTipo().equalsIgnoreCase("LIDER") ? 1 : 4;
        int nueva = Math.max(0, Math.min(max, cantidadEnMazo + delta));
        this.cantidadEnMazo = nueva;
        guardarEnBD(nueva);
        actualizarVista();
        cargarCartasDelMazo(mazoSeleccionado);
        renderDeck(mazoSeleccionado);
    }

    private void actualizarVista() {
        if (lblContador != null && cartaActual != null) {
            lblContador.setText(cartaActual.getNombre() + ": x" + cantidadEnMazo);
            lblContador.setStyle("-fx-text-fill: " + (cantidadEnMazo == 0 ? "gray" : "#2ecc71") + "; -fx-font-weight: bold;");
        }
    }

    private void guardarEnBD(int cant) {
        String sql = cant == 0 ? "DELETE FROM deck_carta WHERE id_deck = ? AND id_carta = ?" 
                               : "INSERT INTO deck_carta (id_deck, id_carta, cantidad) VALUES (?, ?, ?) ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = EXCLUDED.cantidad";
        try (Connection conn = Login.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mazoSeleccionado.getId_deck()); ps.setString(2, cartaActual.getId_carta());
            if (cant > 0) ps.setInt(3, cant);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- UTILIDADES VISUALES Y NAVEGACIÓN ---

    private String calculateGradient(Deck deck) {
        if (deck.getColores().isEmpty()) return "#2c3e50";
        if (deck.getColores().size() == 1) return deck.getColores().get(0);
        return "linear-gradient(to right, " + String.join(", ", deck.getColores()) + ")";
    }

    private String getColorIcons(Deck deck) {
        StringBuilder icons = new StringBuilder();
        for (String c : deck.getColores()) {
            if (c.equals("#e74c3c")) icons.append("🔴"); if (c.equals("#3498db")) icons.append("🔵");
            if (c.equals("#2ecc71")) icons.append("🟢"); if (c.equals("#f1c40f")) icons.append("🟡");
            if (c.equals("#9b59b6")) icons.append("🟣"); if (c.equals("#2c3e50")) icons.append("⚫");
        }
        return icons.toString();
    }

    private void openDeck(Deck deck) {
        mazoSeleccionado = deck;
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/view/deckDetail.fxml"));
            Principal.mostrarVista(view);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void limpiarCamposCreacion() {
        deckNameField.clear(); redColor.setSelected(false); blueColor.setSelected(false);
        greenColor.setSelected(false); yellowColor.setSelected(false);
        if (purpleColor != null) purpleColor.setSelected(false);
        if (blackColor != null) blackColor.setSelected(false);
    }


     @FXML
    private void exportarMazoPDF(ActionEvent event) {
        if (mazoSeleccionado == null || mazoSeleccionado.getCartas().isEmpty()) {
            login.mostrarAlerta("Error", "El mazo está vacío.");
            return;
        }
        Map<String, Integer> conteo = new HashMap<>();
        for (Carta c : mazoSeleccionado.getCartas()) {
            conteo.put(c.getNombre(), conteo.getOrDefault(c.getNombre(), 0) + 1);
        }
        StringBuilder contenido = new StringBuilder();
        contenido.append("<h1>").append(mazoSeleccionado.getNombre_deck()).append("</h1><ul>");
        conteo.forEach((nombre, cantidad) -> contenido.append("<li>").append(cantidad).append("x ").append(nombre).append("</li>"));
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
        } catch (Exception e) { e.printStackTrace(); }
    }


    @FXML private void volver() { try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"))); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void irAColeccionParaEditar() { try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/coleccion.fxml"))); } catch (Exception e) { e.printStackTrace(); } }


}