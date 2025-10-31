package com.joelsitto.bang.model;
import jakarta.persistence.*;

@Entity
@Table(name = "distancies_jugadors")
public class DistanciesJugadors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_distancia")
    private int id_distancia;

    @ManyToOne
    @JoinColumn(name = "id_jugador1", nullable = false)
    private Jugador jugador1;

    @ManyToOne
    @JoinColumn(name = "id_jugador2", nullable = false)
    private Jugador jugador2;

    @Column(name = "distancia", nullable = false)
    private int distancia;

    public DistanciesJugadors() {}

    public DistanciesJugadors(Jugador jugador1, Jugador jugador2, int distancia) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.distancia = distancia;
    }

    // Getters y setters
    public int getId_distancia() {
        return id_distancia;
    }

    public void setId_distancia(int id_distancia) {
        this.id_distancia = id_distancia;
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public void setJugador1(Jugador jugador1) {
        this.jugador1 = jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }

    public void setJugador2(Jugador jugador2) {
        this.jugador2 = jugador2;
    }

    public int getDistancia() {
        return distancia;
    }

    public void setDistancia(int distancia) {
        this.distancia = distancia;
    }
}
