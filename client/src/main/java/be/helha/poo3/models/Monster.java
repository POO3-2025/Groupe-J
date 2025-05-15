package be.helha.poo3.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Monster {
    private Monsters type;
    private int currentHealth;
    private Temperament temperament;
}
