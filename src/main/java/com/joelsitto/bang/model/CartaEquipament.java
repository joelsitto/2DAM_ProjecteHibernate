package com.joelsitto.bang.model;

import com.joelsitto.bang.model.enums.TipusColl;
import com.joelsitto.bang.model.enums.TipusEquipament;
import jakarta.persistence.*;

@Entity
@Table(name = "carta_equipament")
public class CartaEquipament extends Carta {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", length = 30, nullable = false)
    private TipusEquipament tipus;

    @Column(name = "modificador_distancia", nullable = false)
    private int modificadorDistancia;

    @ManyToOne
    @JoinColumn(name = "id_jugador_equipament")
    private Jugador jugadorEquipament;

    public CartaEquipament() {}

    public CartaEquipament(String nom_carta, String descripcio_carta, TipusColl coll, TipusEquipament tipus, int modificadorDistancia) {
        super(nom_carta, descripcio_carta, coll);
        this.tipus = tipus;
        this.modificadorDistancia = modificadorDistancia;
    }

    // Getters y setters
    public TipusEquipament getTipus() {
        return tipus;
    }

    public void setTipus(TipusEquipament tipus) {
        this.tipus = tipus;
    }

    public int getModificadorDistancia() {
        return modificadorDistancia;
    }

    public void setModificadorDistancia(int modificadorDistancia) {
        this.modificadorDistancia = modificadorDistancia;
    }

    public Jugador getJugadorEquipament() {
        return jugadorEquipament;
    }

    public void setJugadorEquipament(Jugador jugadorEquipament) {
        this.jugadorEquipament = jugadorEquipament;
    }
}
