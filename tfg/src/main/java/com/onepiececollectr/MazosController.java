package com.onepiececollectr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Desktop;

public class MazosController {

    @FXML private GridPane deckGrid;
    @FXML private VBox deckList;
    @FXML private Label deckInfoLabel;
    @FXML private TextField deckNameField;
    @FXML private CheckBox redColor, blueColor, greenColor, yellowColor;

    private static List<Deck> misMazos = new ArrayList<>();
    public static Deck mazoSeleccionado = null;
    Login login = new Login();

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
        if (redColor.isSelected()) colors.add("Red");
        if (blueColor.isSelected()) colors.add("Blue");
        if (greenColor.isSelected()) colors.add("Green");
        if (yellowColor.isSelected()) colors.add("Yellow");

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
        // Contenedor horizontal para el mazo y el botón de borrar
        HBox filaMazo = new HBox(10); 
        filaMazo.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5;");

        // 1. Botón principal del mazo
        Button btnMazo = new Button(deck.getNombre_deck() + " " + getColorIcons(deck));
        btnMazo.setPrefWidth(220);
        btnMazo.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;", 
                         calculateGradient(deck)));
        btnMazo.setOnAction(e -> openDeck(deck));

        // 2. Botón de borrar (la X roja)
        Button btnBorrar = new Button("🗑"); // Puedes poner "X" si no te sale el icono
        btnBorrar.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10;");
        
        // Al pulsar, llama al método de borrar que creamos arriba
        btnBorrar.setOnAction(e -> borrarMazo(deck));

        // Añadimos ambos al HBox y el HBox a la lista principal
        filaMazo.getChildren().addAll(btnMazo, btnBorrar);
        deckList.getChildren().add(filaMazo);
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
    
@FXML
private void irAColeccionParaEditar() {
    // IMPORTANTE: mazoSeleccionado ya tiene el mazo que abriste antes
    if (mazoSeleccionado == null) return;

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/coleccion.fxml"));
        Parent root = loader.load();
        Principal.mostrarVista(root);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// También añade el método goBack que pide tu FXML si no lo tienes:
@FXML
private void goBack() { 
    // Muy importante: si volvemos atrás, dejamos de "editar" el mazo
    mazoSeleccionado = null; 
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mazos.fxml"));
        Principal.mostrarVista(loader.load());
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void borrarMazo(Deck mazo) {
    // 1. Confirmación simple (opcional, pero recomendada)
    Login.registrarEnLog("Intentando borrar mazo: " + mazo.getNombre_deck());

    String sql = "DELETE FROM deck WHERE id_deck = ?";
    
    try (Connection conn = Login.getConexion();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, mazo.getId_deck());
        int filasAfectadas = pstmt.executeUpdate();

        if (filasAfectadas > 0) {
            // 2. Si se borró de la BD, lo quitamos de nuestra lista local
            misMazos.remove(mazo);
            
            // 3. Refrescamos la interfaz para que desaparezca el botón
            refreshDeckList();
            
            new Login().registrarEnLog("Mazo '" + mazo.getNombre_deck() + "' eliminado.");
        }
    } catch (SQLException e) {
        System.err.println("Error al borrar mazo: " + e.getMessage());
        new Login().mostrarAlerta("Error", "No se pudo borrar el mazo de la base de datos.");
    }
}

@FXML
private void exportarMazoPDF(ActionEvent event) {
    Deck mazo = MazosController.mazoSeleccionado;
    if (mazo == null || mazo.getCartas().isEmpty()) {
        login.mostrarAlerta("Error", "El mazo está vacío.");
        return;
    }

    // Agrupamos cartas por nombre para contar cantidades (ej: 4x Monkey D. Luffy)
    Map<String, Integer> conteoCartas = new HashMap<>();
    for (Carta c : mazo.getCartas()) {
        conteoCartas.put(c.getNombre(), conteoCartas.getOrDefault(c.getNombre(), 0) + 1);
    }

    // Generamos el contenido para el PDF
    StringBuilder contenido = new StringBuilder();
    contenido.append("<h1>Lista de Mazo: ").append(mazo.getNombre_deck()).append("</h1>");
    contenido.append("<ul>");
    conteoCartas.forEach((nombre, cantidad) -> {
        contenido.append("<li><strong>").append(cantidad).append("x</strong> ").append(nombre).append("</li>");
    });
    contenido.append("</ul>");

    
    generarDocumentoPDF(mazo.getNombre_deck(), contenido.toString());
}

private void generarDocumentoPDF(String nombreMazo, String htmlContenido) {
    // Creamos un nombre de archivo limpio (sin espacios raros)
    String nombreArchivo = "Lista_" + nombreMazo.replaceAll("\\s+", "_") + ".html";
    File file = new File(nombreArchivo);

    try (FileWriter writer = new FileWriter(file)) {
        // Le damos un poco de estilo CSS para que parezca un documento oficial
        String htmlCompleto = "<html><head><style>" +
                "body { font-family: 'Segoe UI', sans-serif; padding: 40px; color: #2c3e50; }" +
                "h1 { color: #e74c3c; border-bottom: 2px solid #e74c3c; padding-bottom: 10px; }" +
                "ul { list-style: none; padding: 0; }" +
                "li { padding: 10px; border-bottom: 1px solid #ecf0f1; font-size: 18px; }" +
                "b { color: #e74c3c; }" +
                ".footer { margin-top: 50px; font-size: 12px; color: #bdc3c7; }" +
                "</style></head><body>" +
                htmlContenido +
                "<div class='footer'>Generado por OnePieceCollectr - 2026</div>" +
                "</body></html>";

        writer.write(htmlCompleto);
        System.out.println("Archivo generado: " + file.getAbsolutePath());

        // Intentamos abrir el archivo automáticamente en el navegador
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(file.toURI());
        } else {
            login.mostrarAlerta("Éxito", "Lista generada en: " + file.getName());
        }

    } catch (IOException e) {
        e.printStackTrace();
        login.mostrarAlerta("Error", "No se pudo generar el archivo de exportación.");
    }
}

}