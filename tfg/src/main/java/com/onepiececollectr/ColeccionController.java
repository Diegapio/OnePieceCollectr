package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColeccionController {

    public static ColeccionController instancia;

    @FXML private GridPane cardGrid;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> tipoFilter;
    @FXML private CheckBox checkPoseidas;
    @FXML private CheckBox checkNoPoseidas;
    @FXML private ComboBox<String> comboCoste;
    @FXML private ComboBox<String> comboContador;
    @FXML private ComboBox<String> comboVida;

    private static final int PAGE_SIZE = 50;

    private Set<String> idsPoseidos = new HashSet<>();
    private List<Carta> cartasCargadas = new ArrayList<>();
    private List<Carta> listaFiltradaActual = new ArrayList<>();
    private int cartasMostradas = 0;
    private int ticketBusqueda = 0;
    private boolean cargandoMas = false;
    Login login = new Login();

    // ── Mapa hex → alias válidos del color (inglés Y español) ───────────────
    // La columna 'color' en carta puede traer "RED", "ROJO", "RED/GREEN", "ROJO/VERDE", etc.
    // Cada entrada del mapa contiene todos los strings que pueden aparecer en la BD para ese color.
    private static final java.util.Map<String, List<String>> HEX_A_COLOR_BD = new java.util.LinkedHashMap<>();
    static {
        HEX_A_COLOR_BD.put("E74C3C", java.util.Arrays.asList("RED",    "ROJO"));
        HEX_A_COLOR_BD.put("3498DB", java.util.Arrays.asList("BLUE",   "AZUL"));
        HEX_A_COLOR_BD.put("2ECC71", java.util.Arrays.asList("GREEN",  "VERDE"));
        HEX_A_COLOR_BD.put("F1C40F", java.util.Arrays.asList("YELLOW", "AMARILLO"));
        HEX_A_COLOR_BD.put("9B59B6", java.util.Arrays.asList("PURPLE", "MORADO", "PÚRPURA"));
        HEX_A_COLOR_BD.put("2C3E50", java.util.Arrays.asList("BLACK",  "NEGRO"));
    }

    public void refrescar() {
        Platform.runLater(() -> filtrarLocalmente(searchField.getText()));
    }

    @FXML
    public void initialize() {
        instancia = this;
        tipoFilter.getItems().addAll("Todos", "LIDER", "PERSONAJE", "EVENTO", "STAGE");
        searchField.textProperty().addListener((obs, viejo, nuevo) -> filtrarLocalmente(nuevo));
        tipoFilter.valueProperty().addListener((obs, viejo, nuevo) -> filtrarLocalmente(searchField.getText()));

        if (comboCoste != null) {
            comboCoste.getItems().addAll("Todos", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            comboCoste.setValue("Todos");
        }
        if (comboContador != null) {
            comboContador.getItems().addAll("Todos", "0", "1000", "2000");
            comboContador.setValue("Todos");
        }
        if (comboVida != null) {
            comboVida.getItems().addAll("Todos", "1000", "2000", "3000", "4000", "5000", "6000",
                    "7000", "8000", "9000", "10000", "11000", "12000", "13000");
            comboVida.setValue("Todos");
        }

        if (comboCoste    != null) comboCoste.valueProperty().addListener((obs, v, n)    -> filtrarLocalmente(searchField.getText()));
        if (comboContador != null) comboContador.valueProperty().addListener((obs, v, n) -> filtrarLocalmente(searchField.getText()));
        if (comboVida     != null) comboVida.valueProperty().addListener((obs, v, n)     -> filtrarLocalmente(searchField.getText()));

        checkPoseidas.selectedProperty().addListener((obs, v, n)    -> handleFiltro());
        checkNoPoseidas.selectedProperty().addListener((obs, v, n)  -> handleFiltro());

        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0.85) cargarMasCartas();
        });

        if (!App.todasLasCartas.isEmpty()) {
            this.cartasCargadas = new ArrayList<>(App.todasLasCartas);
            filtrarLocalmente("");
        }

        new Thread(() -> {
            try {
                if (App.todasLasCartas.isEmpty()) {
                    App.cargarDatosGlobales();
                    this.cartasCargadas = new ArrayList<>(App.todasLasCartas);
                }
                cargarIdsPoseidos();
                Platform.runLater(() -> filtrarLocalmente(""));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void handleFiltro() { filtrarLocalmente(searchField.getText()); }

    @FXML
    private void handleSearch() { filtrarLocalmente(searchField.getText()); }

    // ── Filtrado ──────────────────────────────────────────────────────────────

    private void filtrarLocalmente(String texto) {
        if (cartasCargadas.isEmpty()) return;

        String q          = texto.toLowerCase().trim();
        String tipoSel    = tipoFilter.getValue();
        String costeSel   = comboCoste    != null ? comboCoste.getValue()    : null;
        String contSel    = comboContador != null ? comboContador.getValue() : null;
        String vidaSel    = comboVida     != null ? comboVida.getValue()     : null;

        // Colores legales del mazo en formato BD (inglés mayúsculas: "RED", "GREEN"…)
        List<String> coloresLegales = obtenerColoresMazo(MazosController.mazoSeleccionado);

        List<Carta> filtradas = new ArrayList<>();

        for (Carta c : cartasCargadas) {

            // ── Filtro por colores del mazo ──────────────────────────────────
            // Si hay mazo seleccionado, solo mostramos cartas cuyo color
            // tenga AL MENOS UNO de los colores del mazo.
            // "RED/GREEN" con mazo [RED, GREEN] → válida.
            // "PURPLE"    con mazo [RED, GREEN] → fuera.
            if (MazosController.mazoSeleccionado != null && !coloresLegales.isEmpty()) {
                String colorCarta = c.getColor().toUpperCase(); // Ej: "RED", "RED/GREEN"
                boolean colorValido = coloresLegales.stream()
                        .anyMatch(alias -> colorCarta.contains(alias));
                if (!colorValido) continue;
            }

            // ── Filtro texto ─────────────────────────────────────────────────
            boolean coincideTexto = c.getNombre().toLowerCase().contains(q)
                    || c.getId_carta().toLowerCase().contains(q)
                    || c.getTexto().toLowerCase().contains(q)
                    || c.getSubtipos().toLowerCase().contains(q)
                    || c.getAtributo().toLowerCase().contains(q);
            if (!coincideTexto) continue;

            // ── Filtro posesión ──────────────────────────────────────────────
            boolean esPoseida = idsPoseidos.contains(c.getId_carta());
            if (checkPoseidas.isSelected()   && !checkNoPoseidas.isSelected() && !esPoseida)  continue;
            if (checkNoPoseidas.isSelected() && !checkPoseidas.isSelected()   &&  esPoseida)  continue;

            // ── Filtros numéricos ────────────────────────────────────────────
            if (costeSel != null && !costeSel.equals("Todos") && c.getCoste()    != Integer.parseInt(costeSel)) continue;
            if (contSel  != null && !contSel.equals("Todos")  && c.getContador() != Integer.parseInt(contSel))  continue;
            if (vidaSel  != null && !vidaSel.equals("Todos")  && c.getPoder()    != Integer.parseInt(vidaSel))  continue;

            // ── Filtro tipo ──────────────────────────────────────────────────
            if (tipoSel != null && !tipoSel.equals("Todos") && !c.getTipo().equalsIgnoreCase(tipoSel)) continue;

            filtradas.add(c);
        }

        listaFiltradaActual = filtradas;
        mostrarPrimeraPagina();
    }

    /**
     * Convierte los colores hex del mazo en todos los alias posibles (inglés + español).
     * Mazo con ["#e74c3c", "#2ecc71"] → ["RED", "ROJO", "GREEN", "VERDE"]
     * Una carta con color "RED/GREEN", "ROJO/VERDE" o cualquier combinación hará match.
     */
    private List<String> obtenerColoresMazo(Deck mazo) {
        List<String> colores = new ArrayList<>();
        if (mazo == null || mazo.getColores() == null) return colores;

        for (String colHex : mazo.getColores()) {
            String hexLimpio = colHex.replace("#", "").toUpperCase();
            List<String> alias = HEX_A_COLOR_BD.get(hexLimpio);
            if (alias != null) colores.addAll(alias);
        }
        return colores;
    }

    // ── Paginación ────────────────────────────────────────────────────────────

    private void mostrarPrimeraPagina() {
        ticketBusqueda++;
        int miTicket = ticketBusqueda;
        cargandoMas = false;
        int fin = Math.min(PAGE_SIZE, listaFiltradaActual.size());
        cartasMostradas = fin;

        Platform.runLater(() -> {
            if (miTicket != ticketBusqueda || cardGrid == null) return;
            cardGrid.getChildren().clear();
            for (int i = 0; i < fin; i++) {
                cardGrid.add(createCard(listaFiltradaActual.get(i)), i % 4, i / 4);
            }
        });
    }

    private void cargarMasCartas() {
        if (cargandoMas || cartasMostradas >= listaFiltradaActual.size()) return;
        cargandoMas = true;
        int miTicket = ticketBusqueda;
        int desde = cartasMostradas;
        int hasta = Math.min(desde + PAGE_SIZE, listaFiltradaActual.size());
        cartasMostradas = hasta;

        Platform.runLater(() -> {
            if (miTicket != ticketBusqueda) { cargandoMas = false; return; }
            for (int i = desde; i < hasta; i++) {
                cardGrid.add(createCard(listaFiltradaActual.get(i)), i % 4, i / 4);
            }
            cargandoMas = false;
        });
    }

    // ── Creación de tarjeta visual ────────────────────────────────────────────

    private VBox createCard(Carta cardData) {
        ImageView image = new ImageView();
        try {
            image.setImage(App.getImagen(cardData.getImagen_url()));
        } catch (Exception e) {
            System.err.println("Error cargando imagen: " + e.getMessage());
        }
        image.setFitWidth(105);
        image.setFitHeight(145);

        Label name = new Label(cardData.getNombre());
        name.setWrapText(true);
        name.setStyle("-fx-font-size: 10px; -fx-alignment: center; -fx-text-alignment: center;");

        VBox card = new VBox(5, image, name);
        card.setPrefSize(130, 190);
        card.setCursor(javafx.scene.Cursor.HAND);

        actualizarEstiloCarta(card, cardData);

        card.setOnMouseClicked(event -> {
            // ── Modo selección mercado ────────────────────────────────────
            if (MarketController.modoSeleccionMercado) {
                if (MarketController.listaParaOptimizar.contains(cardData)) {
                    MarketController.listaParaOptimizar.remove(cardData);
                    actualizarEstiloCarta(card, cardData);
                } else {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        borrarDeMiColeccion(cardData.getId_carta());
                        actualizarEstiloCarta(card, cardData);
                    } else {
                        Deck mazo = MazosController.mazoSeleccionado;
                        if (mazo != null) {
                            if (!puedeAnadirAlMazo(cardData, mazo)) return;
                            abrirSelectorDeCopias(cardData);
                        } else {
                            registrarEnColeccion(cardData.getId_carta());
                            actualizarEstiloCarta(card, cardData);
                        }
                    }
                }
                return;
            }

            // ── Lógica normal ────────────────────────────────────────────
            if (event.getButton() == MouseButton.SECONDARY) {
                borrarDeMiColeccion(cardData.getId_carta());
                actualizarEstiloCarta(card, cardData);
            } else {
                if (MazosController.mazoSeleccionado != null) {
                    if (!puedeAnadirAlMazo(cardData, MazosController.mazoSeleccionado)) return;
                    abrirSelectorDeCopias(cardData);
                } else {
                    registrarEnColeccion(cardData.getId_carta());
                    actualizarEstiloCarta(card, cardData);
                }
            }
        });

        return card;
    }

    /**
     * Comprueba las reglas del TCG antes de abrir el selector de copias.
     * Devuelve true si se puede proceder, false si hay que bloquear (ya muestra alerta).
     */
    private boolean puedeAnadirAlMazo(Carta carta, Deck mazo) {
        boolean esLider = "LIDER".equalsIgnoreCase(carta.getTipo());
        if (!esLider) return true; // Los no-líderes siempre pueden intentar añadirse (el límite de 4 lo gestiona CardDetailController)

        // Es un líder: ¿hay ya un líder DIFERENTE en el mazo?
        boolean hayLiderDistinto = mazo.getCartas().stream()
                .anyMatch(c -> "LIDER".equalsIgnoreCase(c.getTipo())
                        && !c.getId_carta().equals(carta.getId_carta()));
        if (hayLiderDistinto) {
            login.mostrarAlerta("Regla de Líder", "El mazo ya tiene un líder. Quítalo primero para poner otro.");
            return false;
        }
        return true;
    }

    private void abrirSelectorDeCopias(Carta carta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cardPopup.fxml"));
            Parent root = loader.load();

            CardDetailController controller = loader.getController();
            controller.cargarDatos(carta);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Gestionar Copias - " + carta.getNombre());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (MazosController.mazoSeleccionado != null) {
                new MazosController().cargarCartasDelMazo(MazosController.mazoSeleccionado);
            }
            filtrarLocalmente(searchField.getText());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Estilos visuales de carta ─────────────────────────────────────────────

    private void actualizarEstiloCarta(VBox card, Carta c) {
        if (card == null || c == null) return;

        Deck mazo = MazosController.mazoSeleccionado;

        long copias = (mazo == null) ? 0 : mazo.getCartas().stream()
                .filter(mc -> mc.getId_carta().equals(c.getId_carta()))
                .count();

        boolean esLider = "LIDER".equalsIgnoreCase(c.getTipo());
        int limite = esLider ? 1 : 4;

        boolean hayLiderDistinto = (mazo != null) && mazo.getCartas().stream()
                .anyMatch(cart -> "LIDER".equalsIgnoreCase(cart.getTipo())
                        && !cart.getId_carta().equals(c.getId_carta()));

        // 🔵 Modo Mercado seleccionada
        if (MarketController.modoSeleccionMercado && MarketController.listaParaOptimizar.contains(c)) {
            card.setStyle("-fx-background-color: #3498db; -fx-border-color: #2980b9; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
            card.setOpacity(1.0);
        }
        // 🚫 Líder bloqueado (hay otro líder distinto en el mazo)
        else if (esLider && hayLiderDistinto) {
            card.setStyle("-fx-background-color: #fadbd8; -fx-border-color: #c0392b; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
            card.setOpacity(0.4);
        }
        // 🔴 Límite de copias alcanzado
        else if (copias >= limite) {
            card.setStyle("-fx-background-color: #fadbd8; -fx-border-color: #c0392b; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
            card.setOpacity(1.0);
        }
        // 🟢 En el mazo, aún hay hueco
        else if (copias > 0) {
            card.setStyle("-fx-background-color: #d4efdf; -fx-border-color: #27ae60; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
            card.setOpacity(1.0);
        }
        // 🟡 Poseída, no en el mazo
        else if (idsPoseidos.contains(c.getId_carta())) {
            card.setStyle("-fx-background-color: white; -fx-border-color: #f1c40f; -fx-border-width: 2; -fx-padding: 5; -fx-alignment: center;");
            card.setOpacity(1.0);
        }
        // ⚪ No poseída
        else {
            card.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-padding: 5; -fx-alignment: center;");
            card.setOpacity(0.4);
        }
    }

    // ── BD ────────────────────────────────────────────────────────────────────

    private void cargarIdsPoseidos() {
        idsPoseidos.clear();
        String sql = "SELECT id_carta FROM coleccion WHERE id_usuario = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) idsPoseidos.add(rs.getString("id_carta"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void registrarEnColeccion(String idCarta) {
        String sql = "INSERT INTO coleccion (id_usuario, id_carta, cantidad) VALUES (?, ?, 1) ON CONFLICT DO NOTHING";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setString(2, idCarta);
            pstmt.executeUpdate();
            idsPoseidos.add(idCarta);
            Login.registrarEnLog("Carta añadida a colección: " + idCarta);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void borrarDeMiColeccion(String idCarta) {
        String sql = "DELETE FROM coleccion WHERE id_usuario = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setString(2, idCarta);
            pstmt.executeUpdate();
            idsPoseidos.remove(idCarta);
            Login.registrarEnLog("Carta eliminada de colección: " + idCarta);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        MazosController.mazoSeleccionado = null;
        try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"))); }
        catch (Exception e) { e.printStackTrace(); }
    }
}