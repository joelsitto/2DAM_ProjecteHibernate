package com.joelsitto.bang.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(name = "actiu", nullable = false)
    private boolean actiu;

    @ManyToOne
    @JoinColumn(name = "id_jugador_actual")
    private Jugador jugadorActual;

    @ManyToMany
    @JoinTable(name = "partida_jugadors",
            joinColumns = @JoinColumn(name = "id_partida"),
            inverseJoinColumns = @JoinColumn(name = "id_jugador"))
    @JsonManagedReference
    private List<Jugador> jugadors = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "pila_robar",
            joinColumns = @JoinColumn(name = "id_partida"),
            inverseJoinColumns = @JoinColumn(name = "id_carta"))
    @JsonManagedReference
    private List<Carta> pilaRobar = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "pila_descartades",
            joinColumns = @JoinColumn(name = "id_partida"),
            inverseJoinColumns = @JoinColumn(name = "id_carta"))
    @JsonManagedReference
    private List<Carta> pilaDescartades = new ArrayList<>();

    public Partida() {}

    public Partida(String estat, Date dataInici, boolean actiu) {
        this.estat = estat;
        this.dataInici = dataInici;
        this.actiu = actiu;
    }

    // Getters y setters
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

    public boolean isActiu() {
        return actiu;
    }

    public void setActiu(boolean actiu) {
        this.actiu = actiu;
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

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public void setJugadorActual(Jugador jugadorActual) {
        this.jugadorActual = jugadorActual;
    }
}
