package com.joelsitto.bang.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carta_arma")
public class CartaArma extends Carta {

    @Column(name = "distancia_arma", nullable = false)
    private int distanciaArma;

    @OneToOne(mappedBy="armaEquipada")
    private Jugador jugadorArma;

    public CartaArma() {}

    // Getters y setters
    public int getDistanciaArma() {
        return distanciaArma;
    }

    public void setDistanciaArma(int distanciaArma) {
        this.distanciaArma = distanciaArma;
    }

    public Jugador getJugadorArma() {
        return jugadorArma;
    }

    public void setJugadorArma(Jugador jugadorArma) {
        this.jugadorArma = jugadorArma;
    }
}
