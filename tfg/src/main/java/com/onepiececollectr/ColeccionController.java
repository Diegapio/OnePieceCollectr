package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
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

    private Set<String> idsPoseidos = new HashSet<>();
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
                this.cartasCargadas = cargarCartasDesdeBD();
                Platform.runLater(() -> filtrarLocalmente(""));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private List<Carta> cargarCartasDesdeBD() {
        List<Carta> lista = new ArrayList<>();
        String sql = "SELECT * FROM carta";
        try (Connection conn = Login.getConexion(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Carta(rs.getString("id_carta"), rs.getString("nombre"),
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
            if (!c.getNombre().toLowerCase().contains(q) && !c.getId_carta().toLowerCase().contains(q)) continue;
            if (tipoSeleccionado != null && !tipoSeleccionado.equals("Todos") && !tipoSeleccionado.equalsIgnoreCase(c.getTipo())) continue;

            if (mazo != null && !coloresPermitidos.isEmpty()) {
                String colorCarta = (c.getColor() != null) ? c.getColor().toUpperCase() : "";
                boolean coincideColor = false;
                for (String colP : coloresPermitidos) {
                    if (colorCarta.contains(colP)) { coincideColor = true; break; }
                }
                if (!coincideColor) continue;
            }
            filtradas.add(c);
        }
        pintaCartas(filtradas);
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

    private void pintaCartas(List<Carta> lista) {
        ticketBusqueda++;
        int miTicket = ticketBusqueda;
        Platform.runLater(() -> { if (cardGrid != null) cardGrid.getChildren().clear(); });
        new Thread(() -> {
            int col = 0, row = 0;
            for (Carta carta : lista) {
                if (miTicket != ticketBusqueda) return;
                VBox cajaCarta = createCard(carta);
                final int c = col, r = row;
                Platform.runLater(() -> {
                    if (miTicket == ticketBusqueda && cardGrid != null) cardGrid.add(cajaCarta, c, r);
                });
                if (++col == 4) { col = 0; row++; }
            }
        }).start();
    }

    private VBox createCard(Carta cardData) {
        ImageView image = new ImageView();
        try { image.setImage(new Image(cardData.getImagen_url(), 105, 145, true, true)); } catch (Exception e) {}
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
        if (event.getButton() == MouseButton.SECONDARY) {
            borrarDeMiColeccion(cardData.getId_carta());
            actualizarEstiloCarta(card, cardData);
        } else {
            // 💡 TU GRAN IDEA: 
            // Si hay un mazo seleccionado, abrimos el selector de copias
            if (MazosController.mazoSeleccionado != null) {
                abrirSelectorDeCopias(cardData);
            } else {
                // Si NO hay mazo, solo la añadimos a nuestra colección personal
                registrarEnColeccion(cardData.getId_carta());
                actualizarEstiloCarta(card, cardData);
                System.out.println("Añadida a colección personal: " + cardData.getNombre());
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
        
        // --- LA MAGIA DEL REFRESCO ---
        // 1. Esperamos a que el usuario cierre la ventana
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

    // 🔴 ROJO: Si ya hemos llegado al límite (1 para Líder, 4 para el resto) [cite: 81, 82]
    if (copias >= limite) {
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
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void borrarDeMiColeccion(String idCarta) {
        String sql = "DELETE FROM coleccion WHERE id_usuario = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setString(2, idCarta);
            pstmt.executeUpdate(); idsPoseidos.remove(idCarta);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        MazosController.mazoSeleccionado = null;
        try { Principal.mostrarVista(FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"))); } catch (Exception e) { e.printStackTrace(); }
    }
}