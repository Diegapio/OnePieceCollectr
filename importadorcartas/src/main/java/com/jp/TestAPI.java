package com.jp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestAPI {

    public static void insertarCarta(Carta c) throws Exception {

        String sql = "INSERT INTO carta (nombre, tipo, color, rareza, imagen_url) VALUES (?, ?, ?, ?, ?)";

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

    public static void main(String[] args) throws Exception {

        URL url = URI.create("https://optcgapi.com/api/allSetCards/").toURL();

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        String jsonResponse = response.toString();

        ObjectMapper objectMapper = new ObjectMapper();

        Carta[] cartas = objectMapper.readValue(jsonResponse, Carta[].class);

        System.out.println("Total cartas: " + cartas.length);
        System.out.println("Primera carta: " + cartas[0].name);

        Connection conn = Database.conectar();
        System.out.println("Conexión a la base de datos exitosa: " + conn);
        conn.close();

        insertarCarta(cartas[0]);
        System.out.println("Carta insertada: " + cartas[0].name);

        // System.out.println(jsonResponse.substring(0, 700));
    }
}