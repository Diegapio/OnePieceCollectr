package com.jp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Carta {

    public String card_id;

    @JsonProperty("card_name")
    public String name;

    public String color;
    public String rarity;
    public String type;

}