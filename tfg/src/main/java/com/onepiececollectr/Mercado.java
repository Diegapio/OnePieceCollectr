package com.onepiececollectr;

/**
 * Clase que representa una oferta de venta o intercambio en el Mercado.
 */
public class Mercado {
    private int id_publicacion;
    private Carta carta;         // Objeto Carta completo para mostrar imagen y nombre
    private Usuario vendedor;    // Objeto Usuario para saber quién la puso a la venta
    private double precio;
    private String estado;       // "Disponible", "Vendido" o "Reservado"

    /**
     * Constructor para crear una publicación en el mercado.
     * @param id_publicacion ID único de la oferta (de la BD)
     * @param carta Objeto Carta que se vende
     * @param vendedor Objeto Usuario que realiza la venta
     * @param precio Precio asignado a la carta
     * @param estado Estado actual de la venta
     */
    public Mercado(int id_publicacion, Carta carta, Usuario vendedor, double precio, String estado) {
        this.id_publicacion = id_publicacion;
        this.carta = carta;
        this.vendedor = vendedor;
        this.precio = precio;
        this.estado = estado;
    }

    // --- GETTERS Y SETTERS ---

    public int getId_publicacion() { return id_publicacion; }
    public void setId_publicacion(int id_publicacion) { this.id_publicacion = id_publicacion; }

    public Carta getCarta() { return carta; }
    public void setCarta(Carta carta) { this.carta = carta; }

    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

}