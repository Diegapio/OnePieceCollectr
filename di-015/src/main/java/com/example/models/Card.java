package com.example.models;

public class Card {

    private String name;
    private String rarity;
    private String image;

    public Card(String name, String rarity, String image) {
        this.name = name;
        this.rarity = rarity;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getRarity() {
        return rarity;
    }

    public String getImage() {
        return image;
    }
}