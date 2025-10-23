package com.joelsitto.bang.model;
import com.joelsitto.bang.model.enums.TipusUs;
import jakarta.persistence.*;

@Entity
@Table(name = "carta_us")
public class CartaUs extends Carta {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", length = 30, nullable = false)
    private TipusUs tipus;

    public CartaUs() {}

    // Getters y setters
}
