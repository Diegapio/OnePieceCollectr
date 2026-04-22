package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ColeccionController {
    Login login = new Login();

    @FXML private GridPane cardGrid;
    @FXML private TextField searchField; // Asegúrate de que este fx:id esté en tu FXML

    private Set<Integer> idsPoseidos = new HashSet<>();
    
    // Lista para mantener el estado de búsqueda actual y no perder la referencia
    private List<Carta> listaFiltrada = new ArrayList<>();

    @FXML
    public void initialize() {
        // Configuramos el buscador primero
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filtrarCartas(newVal);
            });
        }

        // Hilo de carga inicial
        new Thread(() -> {
            try {
                cargarIdsPoseidos();
                // Mostramos todas las cartas al principio
                actualizarInterfaz(App.todasLasCartas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarIdsPoseidos() {
        idsPoseidos.clear();
        String sql = "SELECT id_carta FROM coleccion WHERE id_usuario = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                idsPoseidos.add(rs.getInt("id_carta"));
            }
        } catch (SQLException e) {
            Login.registrarEnLog("ERROR CARGANDO POSEIDAS: " + e.getMessage());
        }
    }

    /**
     * Lógica de filtrado por Nombre o ID
     */
    private void filtrarCartas(String texto) {
        if (texto == null || texto.isEmpty()) {
            actualizarInterfaz(App.todasLasCartas);
            return;
        }

        String lowerCaseFilter = texto.toLowerCase();

        List<Carta> seleccionadas = App.todasLasCartas.stream()
            .filter(carta -> {
                String nombre = carta.getNombre().toLowerCase();
                String id = String.valueOf(carta.getId_carta());
                return nombre.contains(lowerCaseFilter) || id.contains(lowerCaseFilter);
            })
            .collect(Collectors.toList());

        actualizarInterfaz(seleccionadas);
    }

    /**
     * Método centralizado para mostrar cartas (reemplaza tu antiguo mostrarCartas)
     */
    private void actualizarInterfaz(List<Carta> listaAMostrar) {
        Platform.runLater(() -> cardGrid.getChildren().clear());

        new Thread(() -> {
            int column = 0;
            int row = 0;

            for (Carta carta : listaAMostrar) {
                boolean laTiene = idsPoseidos.contains(carta.getId_carta());
                VBox cardUI = createCard(carta, laTiene);

                final int c = column;
                final int r = row;

                Platform.runLater(() -> {
                    cardGrid.add(cardUI, c, r);
                });

                column++;
                if (column == 4) {
                    column = 0;
                    row++;
                }

                // Pequeño descanso para fluidez
                if (row % 10 == 0 && column == 0) {
                    try { Thread.sleep(5); } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    // --- El resto de tus métodos (createCard, registrarCartaEnBD, volverAlPrincipal) 
    // se mantienen igual, solo asegúrate de que registrarCartaEnBD use idsPoseidos.add(idCarta) ---

    private VBox createCard(Carta cardData, boolean poseida) {
        ImageView image = new ImageView();
        try {
            image.setImage(new Image(cardData.getImagen_url(), 100, 120, true, true));
        } catch (Exception e) {
            System.out.println("Error con imagen de: " + cardData.getNombre());
        }

        image.setFitWidth(100);
        image.setFitHeight(120);

        Label nameLabel = new Label(cardData.getNombre());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");
        nameLabel.setWrapText(true);

        Label rarityLabel = new Label(cardData.getRareza());
        rarityLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");

        VBox card = new VBox(8);
        card.getChildren().addAll(image, nameLabel, rarityLabel);

        if (poseida) {
            aplicarEstiloPoseida(card);
        } else {
            card.setOpacity(0.35);
            card.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-alignment: center;");
        }

        card.setPrefSize(140, 220);
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseClicked(event -> {
            registrarCartaEnBD(cardData.getId_carta());
            card.setOpacity(1.0); 
            aplicarEstiloPoseida(card);
            Login.registrarEnLog("Carta guardada en colección: " + cardData.getNombre());
        });

        return card;
    }

    private void aplicarEstiloPoseida(VBox card) {
        card.setStyle("-fx-background-color: white; -fx-border-color: #f1c40f; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-alignment: center;");
    }

    private void registrarCartaEnBD(int idCarta) {
        if (Login.sesionUsuario == null) return;

        String sql = "INSERT INTO coleccion (id_usuario, id_carta, cantidad) VALUES (?, ?, 1) " +
                     "ON CONFLICT (id_usuario, id_carta) DO NOTHING";

        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Login.sesionUsuario.getId());
            pstmt.setInt(2, idCarta);
            pstmt.executeUpdate();
            idsPoseidos.add(idCarta);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void volverAlPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Parent root = loader.load();
            Principal.mostrarVista(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}