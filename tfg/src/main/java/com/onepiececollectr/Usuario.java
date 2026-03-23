package com.onepiececollectr;

public class Usuario {
    public int id;
    public String nombre;
    public Usuario(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
}