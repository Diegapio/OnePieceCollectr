module com.onepiececollectr {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;

    // Esto permite que el cargador de FXML acceda a tus métodos IniciarSesion y registrarNuevoUsuario
    opens com.onepiececollectr to javafx.fxml;
    
    exports com.onepiececollectr;
}