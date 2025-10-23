package com.joelsitto.bang.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carta_arma")
public class CartaArma extends Carta {

    @Column(name = "distancia", nullable = false)
    private int distancia;

    public CartaArma() {}

    // Getters y setters

    public int getDistancia() {
        return distancia;
    }

    public void setDistancia(int distancia) {
        this.distancia = distancia;
    }
}
