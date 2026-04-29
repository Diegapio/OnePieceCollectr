package com.onepiececollectr;

public class Carta {
    private String id_carta; 
    private String nombre;
    private String tipo;
    private String color;
    private String rareza;
    private String imagen_url;

    // Otro constructor
    public Carta(String id_carta, String nombre, String tipo, String color, String rareza, String imagen_url) {
        this.id_carta = id_carta;
        this.nombre = nombre;
        this.tipo = tipo;
        this.color = color;
        this.rareza = rareza;
        this.imagen_url = imagen_url;
    }

    public Carta(){
        
    }

    // Getters y Setters
    public String getId_carta() { return id_carta; }
    public void setId_carta(String id_carta) { this.id_carta = id_carta; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getRareza() { return rareza; }
    public void setRareza(String rareza) { this.rareza = rareza; }

    public String getImagen_url() { return imagen_url; }
    public void setImagen_url(String imagen_url) { this.imagen_url = imagen_url; }
    
    @Override
    public String toString() {
        return "[" + id_carta + "] " + nombre + " (" + rareza + ")";
    }
}
