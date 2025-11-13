package com.joelsitto.bang.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "jugador")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jugador")
    private int id;

    @Column(name = "nom", length = 50, nullable = false)
    private String nom;

    @Column(name = "vida_actual", nullable = false)
    private int vidaActual;

    @Column(name = "vida_maxima", nullable = false)
    private int vidaMaxima;

    @Column(name = "modificador_dist_def", nullable = false)
    private int modificadorDistanciaDef;

    @Column(name = "modificador_dist_off", nullable = false)
    private int modificadorDistanciaOff;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    @JsonManagedReference
    private Rol rol;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_carta_arma")
    @JsonManagedReference
    private CartaArma armaEquipada;

    @OneToMany(mappedBy = "jugadorEquipament", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<CartaEquipament> equipaments = new ArrayList<>();

    @OneToMany(mappedBy = "jugadorMa", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Carta> ma = new ArrayList<>();

    @OneToMany(mappedBy = "jugador1", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<DistanciesJugadors> distanciesJugadors1 = new ArrayList<>();

    @OneToMany(mappedBy = "jugador2", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<DistanciesJugadors> distanciesJugadors2 = new ArrayList<>();

    @ManyToMany(mappedBy = "jugadors")
    @JsonBackReference
    private List<Partida> partides = new ArrayList<>();

    public Jugador() {}

    public Jugador(String nom, int vidaActual, int vidaMaxima, Rol rol) {
        this.nom = nom;
        this.vidaActual = vidaActual;
        this.vidaMaxima = vidaMaxima;
        this.rol = rol;
    }
    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getVidaActual() {
        return vidaActual;
    }

    public void setVidaActual(int vidaActual) {
        this.vidaActual = vidaActual;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public void setVidaMaxima(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
    }

    public int getModificadorDistanciaDef() {
        return modificadorDistanciaDef;
    }

    public void setModificadorDistanciaDef(int modificadorDistanciaDef) {
        this.modificadorDistanciaDef = modificadorDistanciaDef;
    }

    public int getModificadorDistanciaOff() {
        return modificadorDistanciaOff;
    }

    public void setModificadorDistanciaOff(int modificadorDistanciaOff) {
        this.modificadorDistanciaOff = modificadorDistanciaOff;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public CartaArma getArmaEquipada() {
        return armaEquipada;
    }

    public void setArmaEquipada(CartaArma armaEquipada) {
        this.armaEquipada = armaEquipada;
    }

    public List<CartaEquipament> getEquipaments() {
        return equipaments;
    }

    public void setEquipaments(List<CartaEquipament> equipaments) {
        this.equipaments = equipaments;
    }

    public List<Carta> getMa() {
        return ma;
    }

    public void setMa(List<Carta> ma) {
        this.ma = ma;
    }

    public List<DistanciesJugadors> getDistanciesJugadors1() {
        return distanciesJugadors1;
    }

    public void setDistanciesJugadors1(List<DistanciesJugadors> distanciesJugadors1) {
        this.distanciesJugadors1 = distanciesJugadors1;
    }

    public List<DistanciesJugadors> getDistanciesJugadors2() {
        return distanciesJugadors2;
    }

    public void setDistanciesJugadors2(List<DistanciesJugadors> distanciesJugadors2) {
        this.distanciesJugadors2 = distanciesJugadors2;
    }

    public List<Partida> getPartides() {
        return partides;
    }

    public void setPartides(List<Partida> partides) {
        this.partides = partides;
    }
}
