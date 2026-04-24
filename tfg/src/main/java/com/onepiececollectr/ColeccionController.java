package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColeccionController {
    Login login = new Login();

    @FXML private GridPane cardGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> tipoFilter;  

    private Set<Integer> idsPoseidos = new HashSet<>();
    private List<Carta> cartasCargadas = new ArrayList<>(); 
    private int ticketBusqueda = 0; 

    @FXML
    public void initialize() {
    tipoFilter.getItems().addAll("Todos", "LIDER", "PERSONAJE", "EVENTO", "STAGE");
    searchField.textProperty().addListener((obs, viejo, nuevo) -> filtrarLocalmente(nuevo));
    tipoFilter.valueProperty().addListener((obs, viejo, nuevo) -> filtrarLocalmente(searchField.getText()));
        new Thread(() -> {
            try {
                cargarIdsPoseidos();
                // USAMOS CARGA TOTAL (Igual que en tu vista de colección normal)
                this.cartasCargadas = cargarCartasDesdeBD();
                
                Platform.runLater(() -> {
                    filtrarLocalmente(""); 
                    searchField.textProperty().addListener((obs, viejo, nuevo) -> filtrarLocalmente(nuevo));
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private List<Carta> cargarCartasDesdeBD() {
        List<Carta> lista = new ArrayList<>();
        // SIN WHERE: Cargamos todo para que no haya fallos de idioma/colores en SQL
        String sql = "SELECT * FROM carta";
        try (Connection conn = Login.getConexion(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Carta(rs.getInt("id_carta"), rs.getString("nombre"),
                    rs.getString("tipo"), rs.getString("color"), rs.getString("rareza"), rs.getString("imagen_url")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

private void filtrarLocalmente(String texto) {
    if (cartasCargadas.isEmpty()) return;
    
    String q = texto.toLowerCase().trim();
    String tipoSeleccionado = tipoFilter.getValue();
    Deck mazo = MazosController.mazoSeleccionado;
    List<String> coloresPermitidos = obtenerColoresMazo(mazo);

    List<Carta> filtradas = new ArrayList<>();
    
    for (Carta c : cartasCargadas) {
        // 1. Filtro de Búsqueda (Texto)
        boolean coincideTexto = c.getNombre().toLowerCase().contains(q) || 
                                String.valueOf(c.getId_carta()).contains(q);
        if (!coincideTexto) continue;

        // 2. Filtro de Tipo (ComboBox)
        if (tipoSeleccionado != null && !tipoSeleccionado.equals("Todos")) {
            // Comparamos con el tipo de la carta (asegúrate que en tu BD coincida el texto)
            if (!tipoSeleccionado.equalsIgnoreCase(c.getTipo())) {
                continue;
            }
        }

        // 3. Filtro de Color (Si hay mazo)
        if (mazo != null && !coloresPermitidos.isEmpty()) {
            String colorCarta = (c.getColor() != null) ? c.getColor().toUpperCase() : "";
            boolean coincideColor = false;
            for (String colP : coloresPermitidos) {
                if (colorCarta.contains(colP)) {
                    coincideColor = true;
                    break;
                }
            }
            if (!coincideColor) continue;
        }

        filtradas.add(c);
    }
    
    pintaCartas(filtradas);
}

    // Método auxiliar para traducir los colores del mazo a tu idioma de BD
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
            else if (hex.contains("000000") || hex.contains("BLACK")) colores.add("NEGRO");
        }
        return colores;
    }

    private void pintaCartas(List<Carta> lista) {
        ticketBusqueda++;
        int miTicket = ticketBusqueda;
        Platform.runLater(() -> { if (cardGrid != null) cardGrid.getChildren().clear(); });

        new Thread(() -> {
            int col = 0, row = 0;
            // Aquí aplicamos la regla de Líderes al momento de evaluar el mazo
            Deck mazoActual = MazosController.mazoSeleccionado;
            boolean yaTieneLider = (mazoActual == null) ? false : 
                mazoActual.getCartas().stream().anyMatch(mc -> "L".equalsIgnoreCase(mc.getRareza()));

            for (Carta carta : lista) {
                if (miTicket != ticketBusqueda) return;
                
                VBox cajaCarta = createCard(carta);
                final int c = col, r = row;
                Platform.runLater(() -> {
                    if (miTicket == ticketBusqueda && cardGrid != null) cardGrid.add(cajaCarta, c, r);
                });
                col++;
                if (col == 4) { col = 0; row++; }
                if (row % 30 == 0) { try { Thread.sleep(1); } catch (Exception e) {} }
            }
        }).start();
    }

    private VBox createCard(Carta cardData) {
        ImageView image = new ImageView();
        try { image.setImage(new Image(cardData.getImagen_url(), 105, 145, true, true)); } catch (Exception e) {}
        image.setFitWidth(105); image.setFitHeight(145);
        Label name = new Label(cardData.getNombre());
        name.setWrapText(true);
        name.setStyle("-fx-font-size: 10px; -fx-alignment: center; -fx-text-alignment: center;");

        VBox card = new VBox(5, image, name);
        card.setPrefSize(130, 190);
        card.setCursor(javafx.scene.Cursor.HAND);

        actualizarEstiloCarta(card, cardData);

        card.setOnMouseClicked(event -> {
            Deck mazo = MazosController.mazoSeleccionado;
            if (event.getButton() == MouseButton.SECONDARY) {
                borrarDeMiColeccion(cardData.getId_carta());
                actualizarEstiloCarta(card, cardData);
            } else if (mazo != null) {
                // REGLAS DE MAZO
                boolean yaTieneLider = mazo.getCartas().stream().anyMatch(c -> "L".equalsIgnoreCase(c.getRareza()));
                long copias = mazo.getCartas().stream().filter(mc -> mc.getId_carta() == cardData.getId_carta()).count();

                if ("L".equalsIgnoreCase(cardData.getRareza()) && yaTieneLider) {
                    login.mostrarAlerta("Líder", "Tu mazo ya tiene un capitán.");
                } else if (copias >= 4) {
                    login.mostrarAlerta("Límite", "Máximo 4 copias.");
                } else {
                    meterEnMazo(cardData);
                }
            } else {
                registrarEnColeccion(cardData.getId_carta());
                actualizarEstiloCarta(card, cardData);
            }
        });
        return card;
    }

    private void actualizarEstiloCarta(VBox card, Carta c) {
        Deck mazo = MazosController.mazoSeleccionado;
        long copias = (mazo == null) ? 0 : mazo.getCartas().stream().filter(mc -> mc.getId_carta() == c.getId_carta()).count();
        boolean tieneLider = (mazo == null) ? false : mazo.getCartas().stream().anyMatch(mc -> "L".equalsIgnoreCase(mc.getRareza()));
        boolean esLider = "L".equalsIgnoreCase(c.getRareza());

        if (esLider && tieneLider && copias == 0) {
            card.setStyle("-fx-background-color: #fadbd8; -fx-border-color: #c0392b; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
            card.setOpacity(0.5);
        } else if (copias >= 4) {
            card.setStyle("-fx-background-color: #fadbd8; -fx-border-color: #c0392b; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
            card.setOpacity(1.0);
        } else if (copias > 0) {
            card.setStyle("-fx-background-color: #d4efdf; -fx-border-color: #27ae60; -fx-border-width: 3; -fx-padding: 5; -fx-alignment: center; -fx-background-radius: 5;");
            card.setOpacity(1.0);
        } else if (idsPoseidos.contains(c.getId_carta())) {
            card.setOpacity(1.0);
            card.setStyle("-fx-background-color: white; -fx-border-color: #f1c40f; -fx-border-width: 2; -fx-padding: 5; -fx-alignment: center;");
        } else {
            card.setOpacity(0.4);
            card.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-padding: 5; -fx-alignment: center;");
        }
    }

    private void meterEnMazo(Carta carta) {
        Deck mazo = MazosController.mazoSeleccionado;
        String sql = "INSERT INTO deck_carta (id_deck, id_carta, cantidad) VALUES (?, ?, 1) ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = deck_carta.cantidad + 1";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mazo.getId_deck()); pstmt.setInt(2, carta.getId_carta());
            pstmt.executeUpdate();
            mazo.getCartas().add(carta); 
            Platform.runLater(() -> filtrarLocalmente(searchField.getText()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void cargarIdsPoseidos() {
        idsPoseidos.clear();
        String sql = "SELECT id_carta FROM coleccion WHERE id_usuario = ?";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) idsPoseidos.add(rs.getInt("id_carta"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void registrarEnColeccion(int idCarta) {
        String sql = "INSERT INTO coleccion (id_usuario, id_carta, cantidad) VALUES (?, ?, 1) ON CONFLICT DO NOTHING";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId()); pstmt.setInt(2, idCarta);
            pstmt.executeUpdate(); idsPoseidos.add(idCarta);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void borrarDeMiColeccion(int idCarta) {
        String sql = "DELETE FROM coleccion WHERE id_usuario = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId()); pstmt.setInt(2, idCarta);
            pstmt.executeUpdate(); idsPoseidos.remove(idCarta);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        MazosController.mazoSeleccionado = null; 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Principal.mostrarVista(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }
}