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
            comboVida.getItems().addAll("Todos", "1000", "2000", "3000", "4000", "5000", "6000", "7000", "8000", "9000", "10000", "11000", "12000", "13000");
            comboVida.setValue("Todos");
        }

        if (comboCoste != null)
            comboCoste.valueProperty().addListener((obs, v, n) -> filtrarLocalmente(searchField.getText()));
        if (comboContador != null)
            comboContador.valueProperty().addListener((obs, v, n) -> filtrarLocalmente(searchField.getText()));
        if (comboVida != null)
            comboVida.valueProperty().addListener((obs, v, n) -> filtrarLocalmente(searchField.getText()));

        checkPoseidas.selectedProperty().addListener((obs, v, n) -> handleFiltro());
        checkNoPoseidas.selectedProperty().addListener((obs, v, n) -> handleFiltro());

        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0.85) cargarMasCartas();
        });
        
        // Mostrar cartas inmediatamente (sin info de posesión aún)
        if (!App.todasLasCartas.isEmpty()) {
            this.cartasCargadas = new ArrayList<>(App.todasLasCartas);
            filtrarLocalmente("");
        }

        // Cargar posesión en background y repintar cuando llegue
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
private void handleFiltro() {
    filtrarLocalmente(searchField.getText());
}

@FXML
private void handleSearch() {
    filtrarLocalmente(searchField.getText());
}
    private void filtrarLocalmente(String texto) {
    if (cartasCargadas.isEmpty()) return;
    
    String q = texto.toLowerCase().trim();
    String tipoSel = tipoFilter.getValue();
    String costeSel = comboCoste != null ? comboCoste.getValue() : null;
    String contadorSel = comboContador != null ? comboContador.getValue() : null;
    String vidaSel = comboVida != null ? comboVida.getValue() : null;

    // Obtener colores legales si hay un mazo seleccionado
    List<String> coloresLegales = obtenerColoresMazo(MazosController.mazoSeleccionado);

    List<Carta> filtradas = new ArrayList<>();

    for (Carta c : cartasCargadas) {
        // --- NUEVA LÓGICA DE FILTRO POR COLOR DEL MAZO ---
        if (MazosController.mazoSeleccionado != null && !coloresLegales.isEmpty()) {
            // Si el color de la carta no está en la lista de colores del mazo, la saltamos
            // (Usamos split o contains si la carta tiene varios colores en la BD)
            boolean colorValido = false;
            for (String colLegal : coloresLegales) {
                if (c.getColor().toUpperCase().contains(colLegal)) {
                    colorValido = true;
                    break;
                }
            }
            if (!colorValido) continue;
        }

        
        boolean coincideTexto = c.getNombre().toLowerCase().contains(q) || 
                                c.getId_carta().toLowerCase().contains(q) ||
                                c.getTexto().toLowerCase().contains(q) || 
                                c.getSubtipos().toLowerCase().contains(q) || 
                                c.getAtributo().toLowerCase().contains(q);
        
        if (!coincideTexto) continue;

        // Filtro de Posesión
        boolean esPoseida = idsPoseidos.contains(c.getId_carta());
        if (checkPoseidas.isSelected() && !checkNoPoseidas.isSelected() && !esPoseida) continue;
        if (checkNoPoseidas.isSelected() && !checkPoseidas.isSelected() && esPoseida) continue;

        // Filtros Numéricos
        if (costeSel != null && !costeSel.equals("Todos") && c.getCoste() != Integer.parseInt(costeSel)) continue;
        if (contadorSel != null && !contadorSel.equals("Todos") && c.getContador() != Integer.parseInt(contadorSel)) continue;
        if (vidaSel != null && !vidaSel.equals("Todos") && c.getPoder() != Integer.parseInt(vidaSel)) continue;
        
        // Filtro Tipo
        if (tipoSel != null && !tipoSel.equals("Todos") && !c.getTipo().equalsIgnoreCase(tipoSel)) continue;

        filtradas.add(c);
    }
    listaFiltradaActual = filtradas;
    mostrarPrimeraPagina();
}

    private List<String> obtenerColoresMazo(Deck mazo) {
        List<String> colores = new ArrayList<>();
        if (mazo == null || mazo.getColores() == null) return colores;
        for (String colHex : mazo.getColores()) {
            String hex = colHex.toUpperCase();
            if (hex.contains("E74C3C") || hex.contains("RED")) colores.add("ROJO");
            else if (hex.contains("3498DB") || hex.contains("BLUE")) colores.add("AZUL");
            else if (hex.contains("2ECC71") || hex.contains("GREEN")) colores.add("VERDE");
            else if (hex.contains("F1C40F") || hex.contains("YELLOW")) colores.add("AMARILLO");
            else if (hex.contains("9B59B6") || hex.contains("PURPLE")) colores.add("MORADO");
            else if (hex.contains("2C3E50") || hex.contains("BLACK")) colores.add("NEGRO");
        }
        return colores;
    }

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

    // Pintamos el estilo inicial (Gris, Amarillo, Verde o Rojo)
    actualizarEstiloCarta(card, cardData);

    card.setOnMouseClicked(event -> {
        // --- NUEVA LÓGICA: MODO SELECCIÓN MERCADO ---
        if (MarketController.modoSeleccionMercado) {
            if (MarketController.listaParaOptimizar.contains(cardData)) {
                MarketController.listaParaOptimizar.remove(cardData);
                // Al quitarla, devolvemos su color original de colección/mazo
                actualizarEstiloCarta(card, cardData);
            } else {
                // Solo permitimos seleccionar cartas que el usuario ya posee
                if (idsPoseidos.contains(cardData.getId_carta())) {
                    MarketController.listaParaOptimizar.add(cardData);
                    // Pintamos de AZUL para indicar que está en la lista de compra/venta
                    card.setStyle("-fx-background-color: #3498db; -fx-border-color: #2980b9; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
                    card.setOpacity(1.0);
                } else {
                    new Login().mostrarAlerta("Mercado", "Solo puedes seleccionar cartas que ya posees.");
                }
            }
            return; // Importante: Salimos aquí para no abrir el popup del mazo
        }

        // --- LÓGICA NORMAL (Mazo / Colección) ---
        // --- LÓGICA NORMAL (Mazo / Colección) ---
if (event.getButton() == MouseButton.SECONDARY) {
    borrarDeMiColeccion(cardData.getId_carta());
    actualizarEstiloCarta(card, cardData);
} else {
    // CAMBIO AQUÍ: Verificar si hay un mazo seleccionado
    if (MazosController.mazoSeleccionado != null) {
        // Si hay mazo, pedimos cuántas copias añadir
        abrirSelectorDeCopias(cardData);
    } else {
        // Si NO hay mazo, solo la añadimos a nuestra colección personal
        registrarEnColeccion(cardData.getId_carta());
        actualizarEstiloCarta(card, cardData);
    }
}
    });
    
    return card;
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
        
        // 2. En cuanto se cierra, obligamos al mazo a recargarse de la BD
        if (MazosController.mazoSeleccionado != null) {
            // Necesitamos que MazosController recargue las cartas para que 'copias' sea real
            new MazosController().cargarCartasDelMazo(MazosController.mazoSeleccionado);
        }

        // 3. Volvemos a ejecutar el filtro actual para que se repinten los bordes
        filtrarLocalmente(searchField.getText());

    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void actualizarEstiloCarta(VBox card, Carta c) {
    if (card == null || c == null) return;
    
    Deck mazo = MazosController.mazoSeleccionado;
    
    // Contamos cuántas veces aparece el ID de la carta en la lista del mazo 
    long copias = (mazo == null) ? 0 : mazo.getCartas().stream()
            .filter(mc -> mc.getId_carta().equals(c.getId_carta()))
            .count();
    
    // Verificamos si es un líder [cite: 80]
    boolean esLider = "LIDER".equalsIgnoreCase(c.getTipo());
    int limite = esLider ? 1 : 4;
    // Si la carta está en la lista de optimización de mercado, se mantiene azul
    if (MarketController.modoSeleccionMercado && MarketController.listaParaOptimizar.contains(c)) {
        card.setStyle("-fx-background-color: #3498db; -fx-border-color: #2980b9; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
        card.setOpacity(1.0);
    }
    // 🔴 ROJO: Si ya hemos llegado al límite (1 para Líder, 4 para el resto) [cite: 81, 82]
    else if (copias >= limite) {
        card.setStyle("-fx-background-color: #fadbd8; -fx-border-color: #c0392b; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
        card.setOpacity(1.0);
    } 
    // 🟢 VERDE: Si hay al menos una copia pero no hemos llegado al límite [cite: 83]
    else if (copias > 0 && copias < limite) {
        card.setStyle("-fx-background-color: #d4efdf; -fx-border-color: #27ae60; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
        card.setOpacity(1.0);
    } 
    // 🟡 AMARILLO: Si la tienes en tu colección pero NO en este mazo [cite: 84, 85]
    else if (idsPoseidos.contains(c.getId_carta())) {
        card.setStyle("-fx-background-color: white; -fx-border-color: #f1c40f; -fx-border-width: 2; -fx-padding: 5; -fx-alignment: center;");
        card.setOpacity(1.0);
    } 
    // ⚪ GRIS: No poseída [cite: 86, 87]
    else {
        card.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-padding: 5; -fx-alignment: center;");
        card.setOpacity(0.4);
    }
}

    private void cargarIdsPoseidos() {
        idsPoseidos.clear();
        String sql = "SELECT id_carta FROM coleccion WHERE id_usuario = ?";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) idsPoseidos.add(rs.getString("id_carta"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void registrarEnColeccion(String idCarta) {
        String sql = "INSERT INTO coleccion (id_usuario, id_carta, cantidad) VALUES (?, ?, 1) ON CONFLICT DO NOTHING";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setString(2, idCarta);
            pstmt.executeUpdate(); idsPoseidos.add(idCarta);

            Login.registrarEnLog("Carta añadida a colección: " + idCarta);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void borrarDeMiColeccion(String idCarta) {
        String sql = "DELETE FROM coleccion WHERE id_usuario = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setString(2, idCarta);
            pstmt.executeUpdate(); idsPoseidos.remove(idCarta);

            Login.registrarEnLog("Carta eliminada de colección: " + idCarta);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        MazosController.mazoSeleccionado = null;
        try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"))); } catch (Exception e) { e.printStackTrace(); }
    }
}