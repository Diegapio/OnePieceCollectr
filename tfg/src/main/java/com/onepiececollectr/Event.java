package com.onepiececollectr;

/**
 * Clase que representa un evento o torneo en la aplicación OnePieceCollectr.
 */
public class Event {
    private String name;
    private String date;      // Formato esperado: "dd/MM/yyyy"
    private String location;
    private boolean favorite;

    // Constructor para nuevos eventos
    public Event(String name, String date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.favorite = false; // Por defecto no es favorito
    }

    // --- GETTERS Y SETTERS ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * Opcional: Método para depuración (ayuda a ver datos en consola)
     */
    @Override
    public String toString() {
        return "Evento: " + name + " el " + date + " en " + location + 
               (favorite ? " [⭐]" : "");
    }
}