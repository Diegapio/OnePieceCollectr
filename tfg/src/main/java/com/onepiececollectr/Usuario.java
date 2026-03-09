package com.onepiececollectr;

public class Usuario {
    private int id;
    private String nombre;

    // Este es el constructor que usa el Login para "crear" al usuario tras buscarlo en BD
    public Usuario(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters (necesarios para que otras partes de la app lean los datos)
    public int getId() { return id; }
    public String getNombre() { return nombre; }
}