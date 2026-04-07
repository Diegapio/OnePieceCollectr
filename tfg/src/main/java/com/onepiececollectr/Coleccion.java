package com.onepiececollectr;

public class Coleccion {
    private int idUsuario;
    private String idCarta;
    private int cantidad;

    //constructor, acabo de volver de vacaciones no se si es necesario pero ahí se queda
    public Coleccion(int idUsuario, String idCarta, int cantidad) {
        this.idUsuario = idUsuario;
        this.idCarta = idCarta;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getIdCarta() { return idCarta; }
    public void setIdCarta(String idCarta) { this.idCarta = idCarta; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}   

