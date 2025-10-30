package com.joelsitto.bang.model;

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

    @ManyToMany(mappedBy="pilaRobar")
    private List<Partida> partidasPilaRobar = new ArrayList<>();

    @ManyToMany(mappedBy="pilaDescartades")
    private List<Partida> partidasPilaDescartades = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="id_jugador_ma")
    private Jugador jugadorMa;

    public Carta() {}

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

    public Jugador getJugadorMa() {
        return jugadorMa;
    }

    public void setJugadorMa(Jugador jugadorMa) {
        this.jugadorMa = jugadorMa;
    }
}
