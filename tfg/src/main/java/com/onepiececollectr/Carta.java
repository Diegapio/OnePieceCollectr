package com.onepiececollectr;

public class Carta {
    private String id_carta;
    private String nombre;
    private String tipo;
    private String color;
    private String rareza;
    private String imagen_url;
    private String texto;       // Puede ser NULL
    private Integer coste;      // Puede ser NULL
    private Integer poder;      // Puede ser NULL
    private Integer contador;   // Puede ser NULL
    public String subtipos;     // Puede ser NULL
    public String atributo;     // Puede ser NULL


    public Carta(String id_carta, String nombre, String tipo, String color, String rareza, 
                 String imagen_url, String texto, Integer coste, Integer poder, Integer contador, String subtipos, String atributo) {
        this.id_carta = id_carta;
        this.nombre = nombre;
        this.tipo = tipo;
        this.color = color;
        this.rareza = rareza;
        this.imagen_url = imagen_url;
        this.texto = texto;
        this.coste = coste;
        this.poder = poder;
        this.contador = contador;
        this.subtipos = subtipos;
        this.atributo = atributo;
    }

    // Getters con "Escudo contra Nulos"
    public String getTexto() { return texto != null ? texto : ""; }
    public Integer getCoste() { return coste != null ? coste : 0; }
    public Integer getPoder() { return poder != null ? poder : 0; }
    public Integer getContador() { return contador != null ? contador : 0; }
    public String getSubtipos() { return subtipos != null ? subtipos : ""; }
    public String getAtributo() { return atributo != null ? atributo : ""; }

    // Getters normales para los obligatorios
    public String getId_carta() { return id_carta; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getColor() { return color; }
    public String getRareza() { return rareza; }
    public String getImagen_url() { return imagen_url; }
    

    // Setters
    public void setTexto(String texto) { this.texto = texto; }
    public void setCoste(Integer coste) { this.coste = coste; }
    public void setPoder(Integer poder) { this.poder = poder; }
    public void setContador(Integer contador) { this.contador = contador; }
    public void setSubtipos(String subtipos) { this.subtipos = subtipos; }
    public void setAtributo(String atributo) { this.atributo = atributo; }
}