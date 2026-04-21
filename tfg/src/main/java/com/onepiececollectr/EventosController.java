package com.onepiececollectr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventosController {

    @FXML private TextField searchField;
    @FXML private VBox eventList;
    @FXML private TextField nameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField locationField;
    @FXML private Label selectedEventLabel;

    private Event selectedEvent = null; // Para saber si estamos editando
    private boolean showOnlyFavorites = false;
    
    // Lista temporal de eventos (En el futuro podrías llevarla a la BD)
    private static List<Event> listaEventos = new ArrayList<>();

    @FXML
    public void initialize() {
        // Bloquear fechas pasadas en el DatePicker
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // Escuchar cambios en el buscador
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderEvents());

        renderEvents();
    }

    @FXML
    private void renderEvents() {
        eventList.getChildren().clear();
        String filter = searchField.getText().toLowerCase();

        for (Event event : listaEventos) {
            if (showOnlyFavorites && !event.isFavorite()) continue;
            if (!event.getName().toLowerCase().contains(filter)) continue;

            eventList.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(8);
        
        // --- LÓGICA DE COLORES SEGÚN FECHA ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String styleBase = "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12; -fx-border-width: 2; ";
        
        try {
            LocalDate eventDate = LocalDate.parse(event.getDate(), formatter);
            LocalDate today = LocalDate.now();

            if (eventDate.isBefore(today)) {
                card.setStyle(styleBase + "-fx-background-color: #ffe6e6; -fx-border-color: #e74c3c;"); // Pasado
            } else if (!eventDate.isAfter(today.plusDays(3))) {
                card.setStyle(styleBase + "-fx-background-color: #fff3cd; -fx-border-color: #f1c40f;"); // Próximo
            } else {
                card.setStyle(styleBase + "-fx-background-color: white; -fx-border-color: #bdc3c7;"); // Normal
            }
        } catch (Exception e) {
            card.setStyle(styleBase + "-fx-background-color: white; -fx-border-color: #bdc3c7;");
        }

        // --- CONTENIDO DE LA TARJETA ---
        Label name = new Label(event.getName() + (event.isFavorite() ? " ⭐" : ""));
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label details = new Label("📅 " + event.getDate() + " | 📍 " + event.getLocation());
        
        HBox actions = new HBox(10);
        Button btnDelete = new Button("Eliminar");
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> {
            listaEventos.remove(event);
            renderEvents();
        });

        Button btnEdit = new Button("Editar");
        btnEdit.setOnAction(e -> prepararEdicion(event));

        Button btnFav = new Button(event.isFavorite() ? "Quitar Favorito" : "Hacer Favorito");
        btnFav.setOnAction(e -> {
            event.setFavorite(!event.isFavorite());
            renderEvents();
        });

        actions.getChildren().addAll(btnEdit, btnFav, btnDelete);
        card.getChildren().addAll(name, details, actions);

        return card;
    }

    private void prepararEdicion(Event event) {
        selectedEvent = event;
        nameField.setText(event.getName());
        locationField.setText(event.getLocation());
        datePicker.setValue(LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        selectedEventLabel.setText("Editando: " + event.getName());
    }

    @FXML
    private void saveEvent() {
        String name = nameField.getText();
        String location = locationField.getText();
        LocalDate date = datePicker.getValue();

        if (name.isEmpty() || location.isEmpty() || date == null) {
            selectedEventLabel.setText("⚠️ Rellena todos los campos");
            return;
        }

        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        if (selectedEvent != null) {
            // Actualizar existente
            selectedEvent.setName(name);
            selectedEvent.setLocation(location);
            selectedEvent.setDate(formattedDate);
            selectedEvent = null;
            selectedEventLabel.setText("✅ Evento actualizado");
        } else {
            // Crear nuevo
            listaEventos.add(new Event(name, formattedDate, location));
            selectedEventLabel.setText("✅ Evento guardado");
        }

        limpiarCampos();
        renderEvents();
    }

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
      @FXML
private void volverAlPrincipal(ActionEvent event) {
    try {
       //Botón para volver a atrás y estar en la vista principal para poder navegar guay guay
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
        Parent root = loader.load();
        
        
        Principal.mostrarVista(root);
        
    } catch (Exception e) {
        System.err.println("Error al volver al principal: " + e.getMessage());
        e.printStackTrace();
    }
}
}