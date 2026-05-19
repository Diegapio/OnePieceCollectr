package com.onepiececollectr;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventosController {

    @FXML private TextField searchField;
    @FXML private VBox      eventList;
    @FXML private TextField nameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField locationField;
    @FXML private Label     selectedEventLabel;

    private Event   selectedEvent     = null;
    private boolean showOnlyFavorites = false;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static List<Event> listaEventos = new ArrayList<>();
    public static List<Event>  getListaEventos()              { return listaEventos; }
    public static void         setListaEventos(List<Event> e) { listaEventos = e; }

    // ── Inicialización ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderEvents());

        // ▶ Clave: cargar desde BD en background y pintar al terminar
        cargarEventosDesdeBD(() -> Platform.runLater(this::renderEvents));
    }

    // ── Carga desde BD ────────────────────────────────────────────────────────

    /**
     * Carga los eventos del usuario desde la BD en un hilo de fondo.
     * Cuando termina llama al callback (siempre en background; usa Platform.runLater
     * si necesitas tocar la UI desde él).
     */
    private void cargarEventosDesdeBD(Runnable onDone) {
        new Thread(() -> {
            String sql = "SELECT * FROM eventos WHERE id_usuario = ? ORDER BY fecha";
            try (Connection conn = Login.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Login.sesionUsuario.getId());
                ResultSet rs = ps.executeQuery();

                List<Event> cargados = new ArrayList<>();
                while (rs.next()) {
                    Event ev = new Event(
                        rs.getString("nombre"),
                        rs.getString("fecha"),
                        rs.getString("lugar")
                    );
                    ev.setFavorite(rs.getBoolean("favorito"));
                    cargados.add(ev);
                }
                listaEventos = cargados;
            } catch (SQLException e) {
                e.printStackTrace();
                Login.registrarEnLog("ERROR EVENTOS: " + e.getMessage());
            }
            if (onDone != null) onDone.run();
        }, "eventos-load").start();
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @FXML
    private void renderEvents() {
        eventList.getChildren().clear();
        String filter = searchField.getText() != null ? searchField.getText().toLowerCase() : "";

        for (Event event : listaEventos) {
            if (showOnlyFavorites && !event.isFavorite()) continue;
            if (!event.getName().toLowerCase().contains(filter)) continue;
            eventList.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(8);
        String base = "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:12;-fx-border-width:2;";

        try {
            LocalDate eventDate = LocalDate.parse(event.getDate(), FMT);
            LocalDate today     = LocalDate.now();
            if      (eventDate.isBefore(today))               card.setStyle(base + "-fx-background-color:#ffe6e6;-fx-border-color:#e74c3c;");
            else if (!eventDate.isAfter(today.plusDays(3)))   card.setStyle(base + "-fx-background-color:#fff3cd;-fx-border-color:#f1c40f;");
            else                                               card.setStyle(base + "-fx-background-color:white;-fx-border-color:#bdc3c7;");
        } catch (Exception e) {
            card.setStyle(base + "-fx-background-color:white;-fx-border-color:#bdc3c7;");
        }

        Label name    = new Label(event.getName() + (event.isFavorite() ? " ⭐" : ""));
        name.setStyle("-fx-font-size:16;-fx-font-weight:bold;");
        Label details = new Label("📅 " + event.getDate() + "  |  📍 " + event.getLocation());

        Button btnEdit   = new Button("Editar");
        Button btnFav    = new Button(event.isFavorite() ? "Quitar ⭐" : "Favorito ⭐");
        Button btnDelete = new Button("Eliminar");
        btnDelete.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;");

        btnEdit.setOnAction(e -> prepararEdicion(event));

        btnFav.setOnAction(e -> {
            boolean nuevo = !event.isFavorite();
            String sql = "UPDATE eventos SET favorito = ? WHERE nombre = ? AND id_usuario = ?";
            try (Connection conn = Login.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, nuevo);
                ps.setString(2, event.getName());
                ps.setInt(3, Login.sesionUsuario.getId());
                ps.executeUpdate();
                event.setFavorite(nuevo);
                renderEvents();
                Login.registrarEnLog("Favorito cambiado: " + event.getName());
            } catch (SQLException ex) {
                new Login().mostrarAlerta("Error", "No se pudo actualizar el favorito.");
            }
        });

        btnDelete.setOnAction(e -> {
            String sql = "DELETE FROM eventos WHERE nombre = ? AND id_usuario = ?";
            try (Connection conn = Login.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, event.getName());
                ps.setInt(2, Login.sesionUsuario.getId());
                ps.executeUpdate();
                listaEventos.remove(event);
                renderEvents();
                Login.registrarEnLog("Evento eliminado: " + event.getName());
            } catch (SQLException ex) {
                new Login().mostrarAlerta("Error", "No se pudo eliminar el evento.");
            }
        });

        HBox actions = new HBox(10, btnEdit, btnFav, btnDelete);
        card.getChildren().addAll(name, details, actions);
        return card;
    }

    // ── Guardar / Editar ──────────────────────────────────────────────────────

    private void prepararEdicion(Event event) {
        selectedEvent = event;
        nameField.setText(event.getName());
        locationField.setText(event.getLocation());
        datePicker.setValue(LocalDate.parse(event.getDate(), FMT));
        selectedEventLabel.setText("Editando: " + event.getName());
    }

    @FXML
    private void saveEvent() {
        String    name     = nameField.getText().trim();
        String    location = locationField.getText().trim();
        LocalDate date     = datePicker.getValue();

        if (name.isEmpty() || location.isEmpty() || date == null) {
            selectedEventLabel.setText("⚠️ Rellena todos los campos");
            return;
        }

        String formattedDate = date.format(FMT);

        if (selectedEvent != null) {
            // ── Actualizar ────────────────────────────────────────────────
            String sql = "UPDATE eventos SET nombre=?, fecha=?, lugar=? WHERE nombre=? AND id_usuario=?";
            try (Connection conn = Login.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, formattedDate);
                ps.setString(3, location);
                ps.setString(4, selectedEvent.getName());
                ps.setInt(5, Login.sesionUsuario.getId());
                ps.executeUpdate();

                selectedEvent.setName(name);
                selectedEvent.setDate(formattedDate);
                selectedEvent.setLocation(location);
                selectedEvent = null;
                selectedEventLabel.setText("✅ Evento actualizado");
                Login.registrarEnLog("Evento actualizado: " + name);
            } catch (SQLException e) {
                new Login().mostrarAlerta("Error", "No se pudo actualizar el evento.");
            }

        } else {
            // ── Insertar ──────────────────────────────────────────────────
            String sql = "INSERT INTO eventos (id_usuario, nombre, fecha, lugar, favorito) VALUES (?,?,?,?,?)";
            try (Connection conn = Login.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Login.sesionUsuario.getId());
                ps.setString(2, name);
                ps.setString(3, formattedDate);
                ps.setString(4, location);
                ps.setBoolean(5, false);
                ps.executeUpdate();

                listaEventos.add(new Event(name, formattedDate, location));
                selectedEventLabel.setText("✅ Evento guardado");
                Login.registrarEnLog("Evento creado: " + name);
            } catch (SQLException e) {
                new Login().mostrarAlerta("Error", "No se pudo guardar el evento.");
                e.printStackTrace();
            }
        }

        limpiarCampos();
        renderEvents();
    }

    // ── Otras acciones ────────────────────────────────────────────────────────

    @FXML
    private void toggleFavorites() {
        showOnlyFavorites = !showOnlyFavorites;
        renderEvents();
    }

    private void limpiarCampos() {
        nameField.clear();
        locationField.clear();
        datePicker.setValue(null);
    }
}