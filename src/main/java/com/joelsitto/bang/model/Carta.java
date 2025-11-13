package com.joelsitto.bang.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.joelsitto.bang.model.enums.*;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "carta")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Carta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carta")
    private int id;

    @Column(name = "nom_carta", length = 50, nullable = false)
    private String nom_carta;

    @Column(name = "descripcio_carta", length = 255)
    private String descripcio_carta;

    @Enumerated(EnumType.STRING)
    @Column(name = "coll", length = 30, nullable = false)
    private TipusColl coll;

    @ManyToMany(mappedBy="pilaRobar")
    @JsonBackReference
    private List<Partida> partidasPilaRobar = new ArrayList<>();

    @ManyToMany(mappedBy="pilaDescartades")
    @JsonBackReference
    private List<Partida> partidasPilaDescartades = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="id_jugador_ma")
    @JsonManagedReference
    private Jugador jugadorMa;

    public Carta() {}

    public Carta(String nom_carta, String descripcio_carta, TipusColl coll) {
        this.nom_carta = nom_carta;
        this.descripcio_carta = descripcio_carta;
        this.coll = coll;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom_carta() {
        return nom_carta;
    }

    public void setNom_carta(String nom_carta) {
        this.nom_carta = nom_carta;
    }

    public String getDescripcio_carta() {
        return descripcio_carta;
    }

    public void setDescripcio_carta(String descripcio_carta) {
        this.descripcio_carta = descripcio_carta;
    }

    public List<Partida> getPartidasPilaRobar() {
        return partidasPilaRobar;
    }

    public void setPartidasPilaRobar(List<Partida> partidasPilaRobar) {
        this.partidasPilaRobar = partidasPilaRobar;
    }

    public List<Partida> getPartidasPilaDescartades() {
        return partidasPilaDescartades;
    }

    public void setPartidasPilaDescartades(List<Partida> partidasPilaDescartades) {
        this.partidasPilaDescartades = partidasPilaDescartades;
    }

    public Jugador getJugadorMa() {return jugadorMa;}

    public TipusColl getColl() {
        return coll;
    }

    public void setColl(TipusColl coll) {
        this.coll = coll;
    }
    public void setJugadorMa(Jugador jugadorMa) {
        this.jugadorMa = jugadorMa;
    }
}
