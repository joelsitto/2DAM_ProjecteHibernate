package com.joelsitto.bang.dao;

import org.hibernate.SessionFactory;

/**
 * Interficie per gestionar les operacions de Jugador
 */
public interface IJugadorDAO {

    /**
     * Mostra les cartes de la ma d'un jugador
     */
    void mostrarMaJugador(SessionFactory sessionFactory, int idJugador);

    /**
     * Executa un atac BANG
     */
    void usarBANG(SessionFactory sessionFactory, int idPartida, int idJugadorAtacant, int idJugadorObjectiu);

    /**
     * Descarta una carta
     */
    void descartarCarta(SessionFactory sessionFactory, int idPartida, int idJugador, int idCarta);

    /**
     * Comprova si un jugador ha estat eliminat
     */
    void comprovarEliminacio(SessionFactory sessionFactory, int idPartida, int idJugador);

    /**
     * Roba una carta de la pila
     */
    void robarCarta(SessionFactory sessionFactory, int idPartida, int idJugador);

    /**
     * Finalitza el torn d'un jugador
     */
    void passarTorn(SessionFactory sessionFactory, int idPartida, int idJugador);

    /**
     * Equipa una carta (arma o equipament)
     */
    void equiparCarta(SessionFactory sessionFactory, int idJugador, int idCarta);

    /**
     * Calcula la distancia entre dos jugadors
     */
    int calcularDistancia(SessionFactory sessionFactory, int idJugadorOrigen, int idJugadorDesti);

    /**
     * Comprova si un jugador pot atacar a un altre
     */
    boolean comprovarDistanciaAtac(SessionFactory sessionFactory, int idJugadorAtacant, int idJugadorObjectiu);
}

