package com.jp;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TestAPI {

    public static void main(String[] args) {
        try {
            // 1. Llamar a la API
            URL url = URI.create("https://optcgapi.com/api/allSetCards/").toURL();

            ObjectMapper mapper = new ObjectMapper();
            Carta[] cartas = mapper.readValue(new InputStreamReader(url.openStream()), Carta[].class);

            System.out.println("Total cartas: " + cartas.length);
            System.out.println("Primera carta: " + cartas[0].name);

            // 2. Probar conexión
            Connection conn = Database.conectar();
            System.out.println("Conexión a la base de datos exitosa: " + conn);
            conn.close();

            // 3. Insertar TODAS las cartas
            int count = 0;

            for (Carta c : cartas) {
                insertarCarta(c);
                count++;

                if (count % 100 == 0) {
                    System.out.println("Insertadas: " + count);
                }
            }

            System.out.println("Importación completada 😏");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // INSERTAR CARTA
    // =========================
    public static void insertarCarta(Carta c) throws Exception {

        String sql = "INSERT INTO carta (nombre, tipo, color, rareza, imagen_url) " +
                "VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";

        Connection conn = Database.conectar();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, c.name);
        stmt.setString(2, normalizarTipo(c.type));
        stmt.setString(3, normalizarColor(c.color));
        stmt.setString(4, c.rarity);
        stmt.setString(5, c.imageUrl);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    // =========================
    // NORMALIZAR TIPO
    // =========================
    public static String normalizarTipo(String tipo) {
        if (tipo == null)
            return "UNKNOWN";

        return switch (tipo.toLowerCase()) {
            case "character" -> "PERSONAJE";
            case "event" -> "EVENTO";
            case "stage" -> "STAGE";
            case "leader" -> "LIDER";
            default -> tipo.toUpperCase();
        };
    }

    // =========================
    // NORMALIZAR COLOR
    // =========================
    public static String normalizarColor(String color) {
        if (color == null)
            return "UNKNOWN";

        return switch (color.toLowerCase()) {
            case "red" -> "ROJO";
            case "blue" -> "AZUL";
            case "green" -> "VERDE";
            case "purple" -> "MORADO";
            case "black" -> "NEGRO";
            case "yellow" -> "AMARILLO";
            default -> color.toUpperCase();
        };
    }
}
