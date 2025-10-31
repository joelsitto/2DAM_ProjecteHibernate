package com.joelsitto.bang.model;

import com.joelsitto.bang.model.enums.TipusColl;
import jakarta.persistence.*;

@Entity
@Table(name = "carta_arma")
public class CartaArma extends Carta {

    @Column(name = "distancia_arma", nullable = false)
    private int distanciaArma;

    @OneToOne(mappedBy="armaEquipada")
    private Jugador jugadorArma;

    public CartaArma() {}

    public CartaArma(String nom_carta, String descripcio_carta, TipusColl coll, int distanciaArma) {
        super(nom_carta, descripcio_carta, coll);
        this.distanciaArma = distanciaArma;
    }

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
