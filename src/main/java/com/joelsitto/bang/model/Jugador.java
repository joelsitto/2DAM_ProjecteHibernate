package com.joelsitto.bang.model;

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
    private Rol rol;

    @OneToOne
    @JoinColumn(name = "id_arma")
    private CartaArma armaEquipada;

    @OneToMany
    @JoinTable(name = "jugador_equipament",
            joinColumns = @JoinColumn(name = "id_jugador"),
            inverseJoinColumns = @JoinColumn(name = "id_equipament"))
    private List<CartaEquipament> equipaments = new ArrayList<>();

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

    public List<Jugador> getDistancies() {
        return distancies;
    }

    public void setDistancies(List<Jugador> distancies) {
        this.distancies = distancies;
    }

    @ManyToMany
    @JoinTable(name = "jugador_ma",
            joinColumns = @JoinColumn(name = "id_jugador"),
            inverseJoinColumns = @JoinColumn(name = "id_carta"))
    private List<Carta> ma = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "jugador_distancia",
            joinColumns = @JoinColumn(name = "id_jugador1"),
            inverseJoinColumns = @JoinColumn(name = "id_jugador2"))
    private List<Jugador> distancies = new ArrayList<>();

    public Jugador() {}

    // Getters y setters
}
