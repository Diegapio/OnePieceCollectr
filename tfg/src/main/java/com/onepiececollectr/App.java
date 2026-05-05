package com.onepiececollectr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;
import com.onepiececollectr.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    
    @Override
    public void start(Stage stage) throws Exception {
        new Thread(() -> {
            //Cargamos los datos desde un hilo separado para que no se quede congelado
        cargarDatosGlobales();
    }).start();

       FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/view/login.fxml")
);

        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("One Piece Collectr");
        stage.setScene(scene);
        stage.show();
    }

    //Cargamos todas las cartas y las ponemos en una lista para no volver a hacer consultas
    public static List<Carta> todasLasCartas = new ArrayList<>();

    public static void cargarDatosGlobales() {
        todasLasCartas.clear();
        String sql = "SELECT * FROM carta"; 
        try (Connection conn = Login.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                todasLasCartas.add(new Carta(
                rs.getString("id_carta"),
                rs.getString("nombre"),
                rs.getString("tipo"),
                rs.getString("color"),
                rs.getString("rareza"),
                rs.getString("imagen_url"),
                rs.getString("texto"),
                (Integer) rs.getObject("coste"),
                (Integer) rs.getObject("poder"),
                (Integer) rs.getObject("contador"),
                (String) rs.getString("subtipos"),
                (String) rs.getString("atributo")
            ));
            }
            //Debería de poner 3130 cartas o algo así, una burrada
            Login.registrarEnLog("Cartas cargadas en memoria: " + todasLasCartas.size());   
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
       cargarDatosGlobales();
       launch();
            
    }
}