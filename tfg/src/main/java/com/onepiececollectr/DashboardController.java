package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label cardsCount;

    @FXML
    private Label decksCount;

    @FXML
    private Label eventsCount;

    @FXML
    public void initialize() {

        // Datos simulados por ahora
        cardsCount.setText("124");
        decksCount.setText("3");
        eventsCount.setText("2");

    }
}
