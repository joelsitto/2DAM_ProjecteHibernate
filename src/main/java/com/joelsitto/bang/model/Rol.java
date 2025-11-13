package com.joelsitto.bang.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private int id;

    @Column(name = "objectiu", length = 100, nullable = false)
    private String objectiu;

    @OneToMany(mappedBy = "rol")
    @JsonBackReference
    private List<Jugador> jugadors = new ArrayList<>();

    public Rol() {}

    public Rol(String objectiu) {
        this.objectiu = objectiu;
    }

    // Getters y setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjectiu() {
        return objectiu;
    }

    public void setObjectiu(String objectiu) {
        this.objectiu = objectiu;
    }

    public List<Jugador> getJugadors() {
        return jugadors;
    }

    public void setJugadors(List<Jugador> jugadors) {
        this.jugadors = jugadors;
    }
}
