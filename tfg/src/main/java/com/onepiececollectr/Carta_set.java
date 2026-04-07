package com.onepiececollectr;

public class Carta_set {
    private String id_carta;
    private String id_set;

    public Carta_set(String id_carta, String id_set) {
        this.id_carta = id_carta;
        this.id_set = id_set;
    }

    public String getId_carta() { return id_carta; }
    public void setId_carta(String id_carta) { this.id_carta = id_carta; }

    public String getId_set() { return id_set; }
    public void setId_set(String id_set) { this.id_set = id_set; }
}
