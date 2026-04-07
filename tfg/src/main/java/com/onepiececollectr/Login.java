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
import org.mindrot.jbcrypt.BCrypt;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login implements Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private static Connection conexion;
    private static final String URL_BASEDATOS = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:6543/postgres?prepareThreshold=0";
    private static final String USUARIO = "postgres.yllqjmkurbaatgagapzd";
    private static final String PASSWORD = "DiegoMeCagoEnTuVieja"; 

    private static final String ARCHIVO_LOG = "One_piece.log";
    public static Usuario sesionUsuario; // Variable para mantener el usuario logueado

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conectar();
    }

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conectar();
            }
        } catch (SQLException e) {
            registrarEnLog("Error al verificar conexión: " + e.getMessage());
        }
        return conexion;
    }

    public static void conectar() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL_BASEDATOS, USUARIO, PASSWORD);
                registrarEnLog("Conexión con One Piece DB establecida.");
            }
        } catch (SQLException e) {
            System.err.println("Error fatal de conexión: " + e.getMessage());
            registrarEnLog("ERROR CONEXION: " + e.getMessage());
        }
    }

    @FXML
    public void IniciarSesion(ActionEvent event) {
        
        String nombre = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (nombre.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Error", "Por favor, rellena todos los campos.");
            return;
        }

        Usuario usuarioEncontrado = buscarUsuarioEnBD(nombre, password);

        if (usuarioEncontrado != null) {
            sesionUsuario = usuarioEncontrado;
            registrarEnLog("LOGIN EXITOSO: Usuario " + nombre);
            iraPanrallaPrincipal();
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrectos.");
            registrarEnLog("LOGIN FALLIDO: Datos incorrectos para: " + nombre);
        }
    }

    @FXML
    public void registrarNuevoUsuario(ActionEvent event) {
        String nom = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (nom.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Error", "No puedes registrar campos vacíos.");
            return;
        }

        String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
        String sql = "INSERT INTO usuario (nombre, password) VALUES (?, ?)";
        
        try (Connection conn = getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nom);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();

            registrarEnLog("REGISTRO: Nuevo usuario creado: " + nom);
            mostrarAlerta("Bienvenido", "Cuenta creada. ¡Inicia sesión ahora!");
            
            usernameField.clear();
            passwordField.clear();

        } catch (SQLException e) {
            registrarEnLog("ERROR REGISTRO: " + e.getMessage());
            mostrarAlerta("Error", "Ese nombre de usuario ya existe.");
        }
    }

    private Usuario buscarUsuarioEnBD(String nom, String passwordIntroducido) {
        String query = "SELECT id_usuario, nombre, password FROM usuario WHERE nombre = ?";
        
        try (Connection conn = getConexion();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, nom);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("password");
                    
                    // Verificación robusta con BCrypt
                    if (BCrypt.checkpw(passwordIntroducido, hashGuardado)) {
                        return new Usuario(rs.getInt("id_usuario"), rs.getString("nombre"));
                    }
                }
            }
        } catch (SQLException e) {
            registrarEnLog("ERROR BUSQUEDA BD: " + e.getMessage());
        }
        return null;
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
            System.err.println("No se pudo escribir en el log.");
        }
    }

    public void iraPanrallaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/principal.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            registrarEnLog("ERROR NAVEGACION: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista principal.");
        }
    }
}