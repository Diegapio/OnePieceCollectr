package com.onepiececollectr;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardDetailController {

    @FXML private ImageView cardImage;
    @FXML private Label nameLabel;
    @FXML private Label lblCantidad;
    @FXML private Button btnMas;
    @FXML private Button btnMenos;

    private Carta cartaActual;
    private int cantidadEnMazo = 0;
    Login login = new Login();

    // ── Límites (misma fuente de verdad que MazosController) ─────────────────
    private static final int MAX_COPIAS_LIDER  = 1;
    private static final int MAX_COPIAS_NORMAL = 4;
    private static final int MAX_CARTAS_MAZO   = 50;

    public void cargarDatos(Carta carta) {
        this.cartaActual    = carta;
        nameLabel.setText(carta.getNombre());
        if (carta.getImagen_url() != null) {
            cardImage.setImage(new Image(carta.getImagen_url(), true));
        }
        this.cantidadEnMazo = obtenerCantidadEnMazo(carta.getId_carta());
        actualizarInterfaz();
    }

    // ── Botones ───────────────────────────────────────────────────────────────

    @FXML
    private void handleMas() {
        int deltaAjustado = calcularDeltaPermitido(1);
        if (deltaAjustado <= 0) return;

        cantidadEnMazo += deltaAjustado;
        guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
        if (esLider()) actualizarColoresMazoSegunLider(cartaActual);
        actualizarInterfaz();
    }

    @FXML
    private void handleMenos() {
        if (cantidadEnMazo <= 0) return;
        cantidadEnMazo--;
        guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
        actualizarInterfaz();
    }

    /**
     * Añade hasta el máximo permitido, respetando tanto el límite de copias
     * de la carta (1 líderes / 4 normales) como el hueco restante en el mazo (50).
     * Si el mazo tiene 48 cartas y pides +4, solo añade 2.
     */
    @FXML
    private void handleMas4() {
        int limite = maxCopias();
        int delta = limite - cantidadEnMazo;          // cuántas copias le faltan a esta carta
        int deltaAjustado = calcularDeltaPermitido(delta);
        if (deltaAjustado <= 0) return;

        cantidadEnMazo += deltaAjustado;
        guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
        if (esLider()) actualizarColoresMazoSegunLider(cartaActual);
        actualizarInterfaz();
    }

    /** Quita todas las copias de esta carta del mazo. */
    @FXML
    private void handleMenos4() {
        cantidadEnMazo = 0;
        guardarEnBD(cartaActual.getId_carta(), cantidadEnMazo);
        actualizarInterfaz();
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) lblCantidad.getScene().getWindow();
        stage.close();
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    private int maxCopias() {
        return esLider() ? MAX_COPIAS_LIDER : MAX_COPIAS_NORMAL;
    }

    private boolean esLider() {
        return cartaActual != null && "LIDER".equalsIgnoreCase(cartaActual.getTipo());
    }

    /**
     * Calcula cuántas copias se pueden añadir realmente, respetando:
     *   1. Límite de copias de la carta (1 líder / 4 normal)
     *   2. Hueco disponible en el mazo hasta MAX_CARTAS_MAZO (50)
     *   3. Regla del líder único
     * Devuelve 0 si no se puede añadir nada (y muestra alerta si procede).
     * Devuelve un valor positivo recortado si hay menos hueco del pedido.
     */
    private int calcularDeltaPermitido(int deltaDeseado) {
        if (deltaDeseado <= 0) return 0;

        Deck mazo = MazosController.mazoSeleccionado;

        // Regla líder único
        if (esLider() && mazo != null) {
            boolean hayLiderDistinto = mazo.getCartas().stream()
                    .anyMatch(c -> "LIDER".equalsIgnoreCase(c.getTipo())
                            && !c.getId_carta().equals(cartaActual.getId_carta()));
            if (hayLiderDistinto) {
                login.mostrarAlerta("Regla de Líder", "El mazo ya tiene un líder. Quítalo primero para poner otro.");
                return 0;
            }
        }

        // Límite de copias de la carta
        int limite = maxCopias();
        int maxPorCopia = limite - cantidadEnMazo;
        if (maxPorCopia <= 0) return 0;

        // Hueco disponible en el mazo
        int hueco = (mazo == null) ? Integer.MAX_VALUE : MAX_CARTAS_MAZO - mazo.getCartas().size();
        if (hueco <= 0) {
            login.mostrarAlerta("Límite de Mazo", "El mazo ya tiene " + MAX_CARTAS_MAZO + " cartas.");
            return 0;
        }

        // Delta real: el mínimo entre lo pedido, el hueco de la carta y el hueco del mazo
        int deltaReal = Math.min(deltaDeseado, Math.min(maxPorCopia, hueco));

        if (deltaReal < deltaDeseado) {
            // Informamos sin bloquear: añadimos lo que cabe
            login.mostrarAlerta("Límite de Mazo",
                    "Solo quedan " + hueco + " hueco(s). Se añadirán " + deltaReal + " copia(s).");
        }

        return deltaReal;
    }

    // ── Vista ─────────────────────────────────────────────────────────────────

    private void actualizarInterfaz() {
        lblCantidad.setText("x" + cantidadEnMazo);
        int limite = maxCopias();

        if (cantidadEnMazo >= limite) {
            lblCantidad.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 20;");
            btnMas.setDisable(true);
        } else {
            lblCantidad.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 20;");
            btnMas.setDisable(false);
        }

        btnMenos.setDisable(cantidadEnMazo <= 0);
    }

    // ── BD ────────────────────────────────────────────────────────────────────

    private int obtenerCantidadEnMazo(String idCarta) {
        if (MazosController.mazoSeleccionado == null) return 0;
        String sql = "SELECT cantidad FROM deck_carta WHERE id_deck = ? AND id_carta = ?";
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, MazosController.mazoSeleccionado.getId_deck());
            pstmt.setString(2, idCarta);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("cantidad");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private void guardarEnBD(String idCarta, int nuevaCantidad) {
        if (MazosController.mazoSeleccionado == null) return;

        if (nuevaCantidad == 0) {
            String sql = "DELETE FROM deck_carta WHERE id_deck = ? AND id_carta = ?";
            ejecutarUpdate(sql, MazosController.mazoSeleccionado.getId_deck(), idCarta, -1);
        } else {
            String sql = "INSERT INTO deck_carta (id_deck, id_carta, cantidad) VALUES (?, ?, ?) " +
                         "ON CONFLICT (id_deck, id_carta) DO UPDATE SET cantidad = EXCLUDED.cantidad";
            ejecutarUpdate(sql, MazosController.mazoSeleccionado.getId_deck(), idCarta, nuevaCantidad);
        }
        login.registrarEnLog("Carta " + cartaActual.getNombre() +
                " en mazo " + MazosController.mazoSeleccionado.getNombre_deck() +
                " → cantidad: " + nuevaCantidad);
    }

    // ── Actualizar colores del mazo según el líder ────────────────────────────

    /**
     * Cuando se añade un líder multicolor (ej: BLUE/PURPLE), expande los colores
     * del mazo para incluir todos los colores del líder.
     * Así el filtro de coleccion mostrará cartas de todos esos colores.
     */
    private void actualizarColoresMazoSegunLider(Carta lider) {
        if (MazosController.mazoSeleccionado == null || lider.getColor() == null) return;

        java.util.Map<String, String> COLOR_A_HEX = new java.util.LinkedHashMap<>();
        COLOR_A_HEX.put("RED",      "#e74c3c"); COLOR_A_HEX.put("ROJO",     "#e74c3c");
        COLOR_A_HEX.put("BLUE",     "#3498db"); COLOR_A_HEX.put("AZUL",     "#3498db");
        COLOR_A_HEX.put("GREEN",    "#2ecc71"); COLOR_A_HEX.put("VERDE",    "#2ecc71");
        COLOR_A_HEX.put("YELLOW",   "#f1c40f"); COLOR_A_HEX.put("AMARILLO", "#f1c40f");
        COLOR_A_HEX.put("PURPLE",   "#9b59b6"); COLOR_A_HEX.put("MORADO",   "#9b59b6"); COLOR_A_HEX.put("PÚRPURA", "#9b59b6");
        COLOR_A_HEX.put("BLACK",    "#2c3e50"); COLOR_A_HEX.put("NEGRO",    "#2c3e50");

        List<String> nuevosColores = new ArrayList<>();
        String[] partes = lider.getColor().toUpperCase().split("[/;, ]+");
        for (String parte : partes) {
            String hex = COLOR_A_HEX.get(parte.trim());
            if (hex != null && !nuevosColores.contains(hex)) nuevosColores.add(hex);
        }

        if (nuevosColores.isEmpty()) return;

        MazosController.mazoSeleccionado.setColores(nuevosColores);

        String sql = "UPDATE deck SET colores = ? WHERE id_deck = ?";
        try (java.sql.Connection conn = Login.getConexion();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.join(",", nuevosColores));
            pstmt.setInt(2, MazosController.mazoSeleccionado.getId_deck());
            pstmt.executeUpdate();
            login.registrarEnLog("Colores del mazo actualizados por líder " + lider.getNombre() + ": " + nuevosColores);
            // Refresca la lista de mazos para que el botón muestre el nuevo gradiente
            if (MazosController.instancia != null) MazosController.instancia.refreshDeckList();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }

    private void ejecutarUpdate(String sql, int idMazo, String idCarta, int cant) {
        try (Connection conn = Login.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMazo);
            pstmt.setString(2, idCarta);
            if (cant != -1) pstmt.setInt(3, cant);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}