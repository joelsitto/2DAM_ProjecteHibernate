package com.joelsitto.bang.model;

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

    public CartaEquipament() {}

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
}
