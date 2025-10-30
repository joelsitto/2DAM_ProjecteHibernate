package com.joelsitto.bang.model;
import com.joelsitto.bang.model.enums.TipusUs;
import jakarta.persistence.*;

@Entity
@Table(name = "carta_us")
public class CartaUs extends Carta {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus_us", length = 30, nullable = false)
    private TipusUs tipusUs;

    public CartaUs() {}

    // Getters y setters
    public TipusUs getTipusUs() {
        return tipusUs;
    }

    public void setTipusUs(TipusUs tipusUs) {
        this.tipusUs = tipusUs;
    }
}
