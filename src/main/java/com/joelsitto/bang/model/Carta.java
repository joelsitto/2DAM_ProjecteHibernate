package com.joelsitto.bang.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carta")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Carta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carta")
    private int id;

    @Column(name = "nom", length = 50, nullable = false)
    private String nom;

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

    public String getDescripcio() {
        return descripcio;
    }

    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }

    @Column(name = "descripcio", length = 255)
    private String descripcio;

    public Carta() {}

    // Getters y setters
}
