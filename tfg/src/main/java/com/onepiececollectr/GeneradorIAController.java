package com.onepiececollectr;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

public class GeneradorIAController {
    @FXML private TextField txtLider;
    @FXML private TextArea txtResultado;
    @FXML private ProgressIndicator progress;
    
    private DeckIAService iaService = new DeckIAService();

    @FXML
    private void handleGenerarMazo() {
        String lider = txtLider.getText();
        if (lider.isEmpty()) return;

        progress.setVisible(true);
        txtResultado.setText("Consultando a la IA de One Piece... (esto puede tardar 30s en Render)");

        iaService.llamarIA(lider).thenAccept(response -> {
            Platform.runLater(() -> {
                progress.setVisible(false);
                // Aquí podrías usar GSON para limpiar el JSON, pero de momento vemos el texto bruto
                txtResultado.setText(response);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                progress.setVisible(false);
                txtResultado.setText("Error al conectar: " + ex.getMessage());
            });
            return null;
        });
    }
}
