package com.jp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestAPI {

    public static void main(String[] args) throws Exception {

        URL url = URI.create("https://optcgapi.com/api/allSetCards/").toURL();

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream())
        );

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
        
    }
}