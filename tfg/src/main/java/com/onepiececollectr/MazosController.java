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
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MazosController {

    // Cosas FXML
    @FXML private GridPane deckGrid;
    @FXML private VBox deckList;
    @FXML private Label deckInfoLabel;
    @FXML private Label lblNombreMazo;
    @FXML private TextField deckNameField;
    @FXML private Label lblContador;

    @FXML private CheckBox redColor, blueColor, greenColor, yellowColor, purpleColor, blackColor;
    @FXML private Button btnMazoIA;
    @FXML private void sumarUno()  { cambiarCantidad(1);  }
    @FXML private void restarUno() { cambiarCantidad(-1); }
    @FXML private void sumarMax()  { cambiarCantidad(4);  }
    @FXML private void restarMax() { cambiarCantidad(-4); }

    // Variables de clase
    private Carta cartaActual;
    private int cantidadEnMazo = 0;
    private static List<Deck> misMazos = new ArrayList<>();
    public static Deck mazoSeleccionado = null;
    public static MazosController instancia;
    Login login = new Login();

    // ── Límites de copias según reglas de One Piece TCG ──────────────────────
    private static final int MAX_COPIAS_LIDER    = 1;
    private static final int MAX_COPIAS_NORMAL   = 4;
    private static final int MAX_CARTAS_MAZO     = 50;

    public static List<Deck> getMisMazos() { return misMazos; }

    @FXML
    public void initialize() {
        instancia = this;
        if (misMazos.isEmpty() && Login.sesionUsuario != null) {
            cargarMazosDesdeBD();
        }
        if (deckList != null) {
            refreshDeckList();
            limitarSeleccionColores();
        }
        if (deckGrid != null && mazoSeleccionado != null) {
            cargarCartasDelMazo(mazoSeleccionado);
            renderDeck(mazoSeleccionado);
        }
    }

    private void limitarSeleccionColores() {
        CheckBox[] checks = { redColor, blueColor, greenColor, yellowColor, purpleColor, blackColor };
        for (CheckBox cb : checks) {
            if (cb == null) continue;
            cb.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (!isSelected) return;
                long seleccionados = java.util.Arrays.stream(checks)
                        .filter(c -> c != null && c.isSelected()).count();
                if (seleccionados > 2) {
                    cb.setSelected(false);
                    login.mostrarAlerta("Color", "Solo puedes elegir hasta 2 colores.");
                }
            });
        }
    }

    // ── Gestión de mazos ──────────────────────────────────────────────────────

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
        boolean hayColor = redColor.isSelected() || blueColor.isSelected() ||
                greenColor.isSelected() || yellowColor.isSelected() ||
                (purpleColor != null && purpleColor.isSelected()) ||
                (blackColor != null && blackColor.isSelected());

        if (!hayColor) {
            login.mostrarAlerta("Creación de Mazo", "Debes seleccionar al menos un color para el mazo.");
            return;
        }

        String name = (deckNameField.getText() != null && !deckNameField.getText().trim().isEmpty())
                ? deckNameField.getText().trim() : "Nuevo Mazo";

        List<String> colors = new ArrayList<>();
        if (redColor.isSelected())                              colors.add("#e74c3c");
        if (blueColor.isSelected())                             colors.add("#3498db");
        if (greenColor.isSelected())                            colors.add("#2ecc71");
        if (yellowColor.isSelected())                           colors.add("#f1c40f");
        if (purpleColor != null && purpleColor.isSelected())    colors.add("#9b59b6");
        if (blackColor != null && blackColor.isSelected())      colors.add("#2c3e50");

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
                Login.registrarEnLog("Mazo creado: " + name + " colores " + colors);
            }
        } catch (SQLException e) {
            login.mostrarAlerta("Error", "No se pudo crear el mazo");
        }
    }

    public void refreshDeckList() {
        if (deckList == null) return;
        deckList.getChildren().clear();
        for (Deck deck : misMazos) {
            HBox fila = new HBox(10);
            fila.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5;");
            Button btn = new Button(deck.getNombre_deck() + " " + getColorIcons(deck));
            btn.setPrefWidth(220);
            btn.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;",
                    calculateGradient(deck)));
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
            login.registrarEnLog("Mazo eliminado: " + mazo.getNombre_deck());
        } catch (SQLException e) {
            login.mostrarAlerta("Error", "No se pudo borrar");
        }
    }

    // ── Gestión de cartas dentro del mazo ────────────────────────────────────

    /**
     * Carga las cartas del mazo desde BD.
     * IMPORTANTE: cada id_carta se añade UNA sola vez a la lista interna;
     * la cantidad real se obtiene de la columna 'cantidad' en deck_carta.
     * renderDeck() agrupa por id y muestra el multiplicador correctamente.
     */
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
                int cantidad = rs.getInt("cantidad");
                // Añadimos la carta tantas veces como indica 'cantidad'
                // para que mazo.getCartas().size() refleje el total real (útil para el límite de 50)
                for (int i = 0; i < cantidad; i++) {
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
                            rs.getString("subtipos"),
                            rs.getString("atributo")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void renderDeck(Deck mazo) {
        if (deckGrid == null || mazo == null) return;
        deckGrid.getChildren().clear();

        // Agrupa por id_carta para mostrar multiplicador visual
        Map<String, Integer> conteo  = new LinkedHashMap<>();
        Map<String, Carta>   unicas  = new LinkedHashMap<>();
        for (Carta c : mazo.getCartas()) {
            conteo.put(c.getId_carta(), conteo.getOrDefault(c.getId_carta(), 0) + 1);
            unicas.put(c.getId_carta(), c);
        }

        int col = 0, row = 0;
        for (String id : conteo.keySet()) {
            Carta carta    = unicas.get(id);
            int   cantidad = conteo.get(id);
            VBox cardVisual = createMiniCardConMultiplicador(carta, mazo, cantidad);
            deckGrid.add(cardVisual, col, row);
            if (++col == 4) { col = 0; row++; }
        }

        if (lblNombreMazo != null) lblNombreMazo.setText(mazo.getNombre_deck());
        deckInfoLabel.setText(mazo.getCartas().size() + "/" + MAX_CARTAS_MAZO + " cartas");
    }

    private VBox createMiniCardConMultiplicador(Carta carta, Deck deck, int cantidad) {
        // ── Imagen con badge de cantidad ──────────────────────────────────────
        StackPane stack = new StackPane();
        ImageView img = new ImageView(new Image(carta.getImagen_url(), 88, 122, true, true));
        img.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 6, 0, 0, 3);");
        img.setCursor(javafx.scene.Cursor.HAND);

        img.setOnMouseClicked(e -> {
            this.cartaActual    = carta;
            this.cantidadEnMazo = cantidad;
            actualizarVista();
            abrirVentanaDetalle(carta);
        });

        // Badge de cantidad (color según límite alcanzado)
        boolean esLider  = "LIDER".equalsIgnoreCase(carta.getTipo());
        boolean maxAlcan = (esLider && cantidad >= 1) || (!esLider && cantidad >= 4);
        Label lbl = new Label("x" + cantidad);
        lbl.setStyle(
            "-fx-background-color: " + (maxAlcan ? "#c0392b" : "#1a2c42") + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 2 6;" +
            "-fx-background-radius: 4;"
        );
        StackPane.setAlignment(lbl, javafx.geometry.Pos.BOTTOM_RIGHT);
        stack.getChildren().addAll(img, lbl);

        // ── Nombre ────────────────────────────────────────────────────────────
        Label nombre = new Label(carta.getNombre());
        nombre.setMaxWidth(96);
        nombre.setWrapText(true);
        nombre.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-text-fill: #c8dce8;" +
            "-fx-alignment: center;" +
            "-fx-text-alignment: center;"
        );

        // ── Botón eliminar ────────────────────────────────────────────────────
        Button del = new Button("✕");
        del.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #e74c3c;" +
            "-fx-font-size: 11px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 1 6;"
        );
        del.setOnAction(e -> {
            borrarFilaCarta(deck, carta);
            cargarCartasDelMazo(deck);
            renderDeck(deck);
        });

        // ── Contenedor de la carta ────────────────────────────────────────────
        VBox card = new VBox(4, stack, nombre, del);
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        card.setStyle(
            "-fx-background-color: #1a2c42;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 6 6 6;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0, 0, 2);"
        );
        card.setPrefWidth(108);
        return card;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void borrarFilaCarta(Deck deck, Carta carta) {
        String sql = "DELETE FROM deck_carta WHERE id_deck = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, deck.getId_deck());
            pstmt.setString(2, carta.getId_carta());
            pstmt.executeUpdate();
            Login.registrarEnLog("Carta eliminada del mazo: " + carta.getNombre() + " del mazo " + deck.getNombre_deck());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Lógica de copias (One Piece TCG) ─────────────────────────────────────

    /**
     * Devuelve el máximo de copias permitidas para una carta.
     *   - LIDER  → 1
     *   - resto  → 4
     */
    private int maxCopiasPermitidas(Carta carta) {
        return esLider(carta) ? MAX_COPIAS_LIDER : MAX_COPIAS_NORMAL;
    }

    private boolean esLider(Carta carta) {
        return carta != null && "LIDER".equalsIgnoreCase(carta.getTipo());
    }

    /**
     * Comprueba si el mazo ya tiene un Líder distinto al indicado.
     */
    private boolean hayLiderDistinto(Carta carta) {
        return mazoSeleccionado.getCartas().stream()
                .anyMatch(c -> esLider(c) && !c.getId_carta().equals(carta.getId_carta()));
    }

    /**
     * Punto de entrada de los botones +1 / -1 / +4 / -4.
     * Aplica todas las restricciones del TCG antes de persistir.
     */
    private void cambiarCantidad(int delta) {
        if (mazoSeleccionado == null || cartaActual == null) return;

        int maxPermitido = maxCopiasPermitidas(cartaActual);

        // Regla líder: solo puede haber 1 líder en el mazo, y es único
        if (esLider(cartaActual) && delta > 0 && hayLiderDistinto(cartaActual)) {
            login.mostrarAlerta("Regla de Mazo", "Ya hay un Líder en este mazo. Solo se permite 1.");
            return;
        }

        // Regla tamaño: el mazo no puede superar MAX_CARTAS_MAZO (50) cartas en total
        int totalActual = mazoSeleccionado.getCartas().size();
        int deltaEfectivo = delta; // puede recortarse si choca con el límite del mazo

        if (delta > 0) {
            int hueco = MAX_CARTAS_MAZO - totalActual;
            if (hueco <= 0) {
                login.mostrarAlerta("Regla de Mazo", "El mazo ya tiene " + MAX_CARTAS_MAZO + " cartas.");
                return;
            }
            deltaEfectivo = Math.min(delta, hueco);
        }

        // Calcula la nueva cantidad respetando [0, maxPermitido]
        int nueva = Math.max(0, Math.min(maxPermitido, cantidadEnMazo + deltaEfectivo));

        if (nueva == cantidadEnMazo) return; // nada que cambiar

        // Si se añade un líder, actualiza los colores del mazo automáticamente
        if (esLider(cartaActual) && nueva > 0) {
            actualizarColoresMazoSegunLider(cartaActual);
        }

        cantidadEnMazo = nueva;
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
        String sql = cant == 0
                ? "DELETE FROM deck_carta WHERE id_deck = ? AND id_carta = ?"
                : "INSERT INTO deck_carta (id_deck, id_carta, cantidad) VALUES (?, ?, ?) " +
                  "ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = EXCLUDED.cantidad";
        try (Connection conn = Login.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mazoSeleccionado.getId_deck());
            ps.setString(2, cartaActual.getId_carta());
            if (cant > 0) ps.setInt(3, cant);
            ps.executeUpdate();
            login.registrarEnLog("Carta " + cartaActual.getNombre() +
                    " en mazo " + mazoSeleccionado.getNombre_deck() +
                    " → cantidad: " + cant);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Colores según líder ───────────────────────────────────────────────────

    private void actualizarColoresMazoSegunLider(Carta lider) {
        List<String> nuevosColores = new ArrayList<>();
        String coloresStr = lider.getColor().toUpperCase();

        if (coloresStr.contains("RED")    || coloresStr.contains("ROJO"))     nuevosColores.add("#e74c3c");
        if (coloresStr.contains("BLUE")   || coloresStr.contains("AZUL"))     nuevosColores.add("#3498db");
        if (coloresStr.contains("GREEN")  || coloresStr.contains("VERDE"))    nuevosColores.add("#2ecc71");
        if (coloresStr.contains("YELLOW") || coloresStr.contains("AMARILLO")) nuevosColores.add("#f1c40f");
        if (coloresStr.contains("PURPLE") || coloresStr.contains("MORADO"))   nuevosColores.add("#9b59b6");
        if (coloresStr.contains("BLACK")  || coloresStr.contains("NEGRO"))    nuevosColores.add("#2c3e50");

        if (nuevosColores.isEmpty()) return;

        mazoSeleccionado.setColores(nuevosColores);

        String sql = "UPDATE deck SET colores = ? WHERE id_deck = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.join(",", nuevosColores));
            pstmt.setInt(2, mazoSeleccionado.getId_deck());
            pstmt.executeUpdate();
            login.registrarEnLog("Colores del mazo " + mazoSeleccionado.getNombre_deck() +
                    " actualizados por el líder " + lider.getNombre());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Utilidades visuales y navegación ─────────────────────────────────────

    private String calculateGradient(Deck deck) {
        if (deck.getColores().isEmpty()) return "#2c3e50";
        if (deck.getColores().size() == 1) return deck.getColores().get(0);
        return "linear-gradient(to right, " + String.join(", ", deck.getColores()) + ")";
    }

    private String getColorIcons(Deck deck) {
        StringBuilder icons = new StringBuilder();
        for (String c : deck.getColores()) {
            if (c.equals("#e74c3c")) icons.append("🔴");
            if (c.equals("#3498db")) icons.append("🔵");
            if (c.equals("#2ecc71")) icons.append("🟢");
            if (c.equals("#f1c40f")) icons.append("🟡");
            if (c.equals("#9b59b6")) icons.append("🟣");
            if (c.equals("#2c3e50")) icons.append("⚫");
        }
        return icons.toString();
    }

    private void openDeck(Deck deck) {
        mazoSeleccionado = deck;
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/view/deckDetail.fxml"));
            Principal.mostrarVista(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limpiarCamposCreacion() {
        deckNameField.clear();
        redColor.setSelected(false);    blueColor.setSelected(false);
        greenColor.setSelected(false);  yellowColor.setSelected(false);
        if (purpleColor != null) purpleColor.setSelected(false);
        if (blackColor != null)  blackColor.setSelected(false);
    }

    // ── Export PDF ────────────────────────────────────────────────────────────

    @FXML
    private void exportarMazoPDF(ActionEvent event) {
        if (mazoSeleccionado == null || mazoSeleccionado.getCartas().isEmpty()) {
            login.mostrarAlerta("Error", "El mazo está vacío.");
            return;
        }
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Carta c : mazoSeleccionado.getCartas()) {
            conteo.put(c.getNombre(), conteo.getOrDefault(c.getNombre(), 0) + 1);
        }
        StringBuilder contenido = new StringBuilder();
        contenido.append("<h1>").append(mazoSeleccionado.getNombre_deck()).append("</h1><ul>");
        conteo.forEach((nombre, cantidad) ->
                contenido.append("<li>").append(cantidad).append("x ").append(nombre).append("</li>"));
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

    // ── Navegación ────────────────────────────────────────────────────────────

    @FXML
    private void volver() {
        try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"))); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void irAColeccionParaEditar() {
        try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/coleccion.fxml"))); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // ── Renombrar y eliminar mazo desde deckDetail ────────────────────────────

    @FXML
    private void onClickNombre(MouseEvent e) {
        if (e.getClickCount() != 2 || mazoSeleccionado == null) return;
        TextInputDialog dialog = new TextInputDialog(mazoSeleccionado.getNombre_deck());
        dialog.setTitle("Renombrar mazo");
        dialog.setHeaderText(null);
        dialog.setContentText("Nuevo nombre:");
        dialog.showAndWait().ifPresent(nuevoNombre -> {
            if (!nuevoNombre.isBlank()) renombrarMazo(mazoSeleccionado, nuevoNombre.trim());
        });
    }

    private void renombrarMazo(Deck mazo, String nuevoNombre) {
        String sql = "UPDATE deck SET nombre = ? WHERE id_deck = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setInt(2, mazo.getId_deck());
            ps.executeUpdate();
            mazo.setNombre_deck(nuevoNombre);
            if (lblNombreMazo != null) lblNombreMazo.setText(nuevoNombre);
            Login.registrarEnLog("Mazo renombrado a: " + nuevoNombre);
        } catch (SQLException ex) {
            login.mostrarAlerta("Error", "No se pudo renombrar el mazo.");
        }
    }

    @FXML
    private void eliminarMazoActual() {
        if (mazoSeleccionado == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el mazo \"" + mazoSeleccionado.getNombre_deck() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                borrarMazo(mazoSeleccionado);
                mazoSeleccionado = null;
                try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/mazos.fxml"))); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }
}