module com.onepiececollectr {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires jbcrypt;
    requires java.desktop;
    opens com.onepiececollectr to javafx.fxml;
    
    exports com.onepiececollectr;
}