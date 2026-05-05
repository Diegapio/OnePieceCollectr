package com.onepiececollectr;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Deck {
    private int id_deck;
    private int id_usuario;
    private String nombre_deck;
    private List<String> colores = new ArrayList<>(); 
    private List<Carta> cartas = new ArrayList<>();   

    public Deck(int id_deck, int id_usuario, String nombre_deck) {
        this.id_deck = id_deck;
        this.id_usuario = id_usuario;
        this.nombre_deck = nombre_deck;
    }
    
    // MÉTODO NUEVO: Para convertir la lista de colores a un String para la BD
    public String getColoresComoString() {
        return String.join(",", colores);
    }

    
    public void setColoresDesdeString(String coloresConcat) {
        if (coloresConcat != null && !coloresConcat.isEmpty()) {
            this.colores = new ArrayList<>(Arrays.asList(coloresConcat.split(",")));
        }
    }

    public int getId_deck() { return id_deck; }
    public int getId_usuario() { return id_usuario; }
    public String getNombre_deck() { return nombre_deck; }
    public void setNombre_deck(String nombre_deck) { this.nombre_deck = nombre_deck; }
    public List<String> getColores() { return colores; }
    public void setColores(List<String> colores) { this.colores = colores; }
    public List<Carta> getCartas() { return cartas; }
}