package com.onepiececollectr;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login implements Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    // ATRIBUTOS QUE FALTABAN
    private static Connection conexion;
    private static final String URL_BASEDATOS = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:6543/postgres?prepareThreshold=0";
    private static final String USUARIO = "postgres.yllqjmkurbaatgagapzd";
    private static final String PASSWORD = "OnePiece123-$!"; 
   
    private static final String ARCHIVO_LOG = "sistema_one_piece.log";
    private static Usuario sesionUsuario;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conectar();
    }

    // Método para obtener la conexión
    public static Connection getConexion() {
        if (conexion == null)
            conectar();
        return conexion;
    }

    public static void conectar() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL_BASEDATOS, USUARIO, PASSWORD);
                registrarEnLog("Conexión con One Piece DB establecida.");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a la BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Conexión con el FXML para iniciar sesión
    @FXML
    public void IniciarSesion(ActionEvent event) {
        String nombre = usernameField.getText();
        String contraseña = passwordField.getText();

        if (nombre.isEmpty() || contraseña.isEmpty()) {
            mostrarAlerta("Error", "Por favor, rellena todos los campos.");
            return;
        }

        // llama a la función que busca el usuario en la BD, si lo encuentra lo carga,
        // si no, lo registra
        Usuario usuarioEncontrado = buscarUsuarioEnBD(nombre, contraseña);

        if (usuarioEncontrado != null) {
            sesionUsuario = usuarioEncontrado;
            registrarEnLog("LOGIN: Usuario " + nombre + " ha entrado.");
            // Hay que pasar a la siguiente pestaña una vez registrado pa que vea todo el usuario
            iraPanrallaPrincipal();
        } else {
            // Si no se encuentra, mostramos el error de datos incorrectos
            mostrarAlerta("Error", "Datos del usuario incorrectos.");
            registrarEnLog("LOGIN FALLIDO: Datos incorrectos para el usuario: " + nombre);
        }
    }

    // En caso de que no exista el usuario, este método lo añade, lo carga y lo
    // guarda en el log
    @FXML
    public void registrarNuevoUsuario(ActionEvent event) {
        String nom = usernameField.getText();
        String psw = passwordField.getText();
        //int nuevoId = obtenerSiguienteIdDeBaseDeDatos();

        String sql = "INSERT INTO usuario (nombre, contraseña) VALUES (?, ?)";
        try (PreparedStatement pstmt = getConexion().prepareStatement(sql)) {
            //pstmt.setInt(1, nuevoId);
            pstmt.setString(1, nom);
            pstmt.setString(2, psw);
            pstmt.executeUpdate();

            //sesionUsuario = new Usuario(nuevoId, nom);
            registrarEnLog("REGISTRO: Nuevo usuario creado: " + nom + " con ID: " + obtenerSiguienteIdDeBaseDeDatos());
            mostrarAlerta("Bienvenido", "Cuenta creada con éxito. ¡Hola, " + nom + "!");

            // Comentamos el avance de pantalla para verificar que se ha escrito todo bien
            iraPanrallaPrincipal();

            // Limpiamos los campos para que el usuario pueda escribir de nuevo y probar el login
            usernameField.clear();
            passwordField.clear();

        } catch (SQLException e) {
            registrarEnLog("ERROR REGISTRO: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo registrar al usuario.");
        }
    }

    private Usuario buscarUsuarioEnBD(String nom, String psw) {
        String query = "SELECT id, nombre FROM usuario WHERE nombre = ? AND contraseña = ?";
        try (PreparedStatement pstmt = getConexion().prepareStatement(query)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, psw);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Si existe se crea el objeto Usuario con los datos de la base
                return new Usuario(rs.getInt("id"), rs.getString("nombre"));
            }
        } catch (SQLException e) {
            registrarEnLog("ERROR BUSQUEDA: " + e.getMessage());
        }
        return null;
    }

    // Como el ID queremos que sea incremental para no comernos la cabeza, este
    // método pilla el id max actual y le suma 1
    public int obtenerSiguienteIdDeBaseDeDatos() {
        int siguienteId = 1;
        String query = "SELECT MAX(id) FROM usuario";

        try (Statement stmt = getConexion().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int maxIdActual = rs.getInt(1);
                siguienteId = maxIdActual + 1;
            }
        } catch (SQLException e) {
            registrarEnLog("LOG: No se pudo obtener MAX ID, se asignará ID 1.");
        }
        return siguienteId;
    }

    public void mostrarAlerta(String titulo, String msj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }

    public static void registrarEnLog(String mensaje) {
        try (FileWriter fw = new FileWriter(ARCHIVO_LOG, true);
                PrintWriter pw = new PrintWriter(fw)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println("[" + timestamp + "] " + mensaje);
        } catch (Exception e) {
            System.err.println("Error fatal escribiendo log: " + e.getMessage());
        }
    }

        public void iraPanrallaPrincipal() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/principal.fxml"));
                System.out.println("Recurso FXML: " + getClass().getResource("/view/principal.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                registrarEnLog("ERROR CARGANDO PANTALLA PRINCIPAL: " + e.getMessage());
                mostrarAlerta("Error", "No se pudo cargar la pantalla principal.");
            }
        }
    

}