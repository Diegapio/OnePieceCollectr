package com.onepiececollectr;

public class Deck {
    private int id_deck;
    private int id_usuario;
    private String nombre_deck;

    public Deck(int id_deck, int id_usuario, String nombre_deck) {
        this.id_deck = id_deck;
        this.id_usuario = id_usuario;
        this.nombre_deck = nombre_deck;
    }
    
    public int getId_deck() { return id_deck; }
    public void setId_deck(int id_deck) { this.id_deck = id_deck; }
    public int getId_usuario() { return id_usuario; }   
    public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }
    public String getNombre_deck() { return nombre_deck; }
    public void setNombre_deck(String nombre_deck) { this.nombre_deck = nombre_deck; }

}
