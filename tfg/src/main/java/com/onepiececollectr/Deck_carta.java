package com.onepiececollectr;

public class Deck_carta {
    private int id_deck;
    private String id_carta;
    private int cantidad;

    
    public Deck_carta(int id_deck, String id_carta, int cantidad) {
        this.id_deck = id_deck;
        this.id_carta = id_carta;
        this.cantidad = cantidad;
    }


    public int getId_deck() { return id_deck; }
    public void setId_deck(int id_deck) { this.id_deck = id_deck; }
    public String getId_carta() { return id_carta; }
    public void setId_carta(String id_carta) { this.id_carta = id_carta; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
