package com.onepiececollectr;

public class Set {
    private String id_set;
    private String nombre_set;
    private String codigo_set;

    public Set(String id_set, String nombre_set, String codigo_set) {
        this.id_set = id_set;
        this.nombre_set = nombre_set;
        this.codigo_set = codigo_set;
    }

    public String getId_set() { return id_set; }
    public void setId_set(String id_set) { this.id_set = id_set; }
    public String getNombre_set() { return nombre_set; }
    public void setNombre_set(String nombre_set) { this.nombre_set = nombre_set; }
    public String getCodigo_set(){return codigo_set;}
    public void setCodigo_set(String codigo_set){this.codigo_set = codigo_set;}

    
}
