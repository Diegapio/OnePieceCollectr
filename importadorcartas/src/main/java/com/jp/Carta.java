package com.jp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Carta {

    @JsonProperty("card_set_id")
    public String cardId;

    @JsonProperty("card_name")
    public String name;

    @JsonProperty("card_type")
    public String type;

    @JsonProperty("card_color")
    public String color;

    @JsonProperty("rarity")
    public String rarity;

    @JsonProperty("set_name")
    public String setName;

    @JsonProperty("set_id")
    public String setId;

    @JsonProperty("card_text")
    public String text;

    @JsonProperty("card_cost")
    public String cost;

    @JsonProperty("card_power")
    public String power;

    @JsonProperty("counter_amount")
    public String counter;

    @JsonProperty("attribute")
    public String attribute;

    @JsonProperty("card_image")
    public String imageUrl;

    @JsonProperty("life")
    public String life;

    @JsonProperty("sub_types")
    public String subTypes;
}