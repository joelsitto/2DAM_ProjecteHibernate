package com.joelsitto.bang.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "partida")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partida")
    private int id;

    @Column(name = "estat", length = 50, nullable = false)
    private String estat;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_inici", nullable = false)
    private Date dataInici;

    @ManyToMany
    @JoinTable(name = "partida_jugadors",
            joinColumns = @JoinColumn(name = "id_partida"),
            inverseJoinColumns = @JoinColumn(name = "id_jugador"))
    private List<Jugador> jugadors = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "pila_robar",
            joinColumns = @JoinColumn(name = "id_partida"),
            inverseJoinColumns = @JoinColumn(name = "id_carta"))
    private List<Carta> pilaRobar = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEstat() {
        return estat;
    }

    public void setEstat(String estat) {
        this.estat = estat;
    }

    public Date getDataInici() {
        return dataInici;
    }

    public void setDataInici(Date dataInici) {
        this.dataInici = dataInici;
    }

    public List<Jugador> getJugadors() {
        return jugadors;
    }

    public void setJugadors(List<Jugador> jugadors) {
        this.jugadors = jugadors;
    }

    public List<Carta> getPilaRobar() {
        return pilaRobar;
    }

    public void setPilaRobar(List<Carta> pilaRobar) {
        this.pilaRobar = pilaRobar;
    }

    public List<Carta> getPilaDescartades() {
        return pilaDescartades;
    }

    public void setPilaDescartades(List<Carta> pilaDescartades) {
        this.pilaDescartades = pilaDescartades;
    }

    @ManyToMany
    @JoinTable(name = "pila_descartades",
            joinColumns = @JoinColumn(name = "id_partida"),
            inverseJoinColumns = @JoinColumn(name = "id_carta"))
    private List<Carta> pilaDescartades = new ArrayList<>();

    public Partida() {}

    // Getters y setters
}
