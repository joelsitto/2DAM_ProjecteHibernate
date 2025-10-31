package com.joelsitto.bang.dao;

import com.joelsitto.bang.model.*;
import com.joelsitto.bang.model.enums.TipusColl;
import org.hibernate.SessionFactory;
import java.util.List;

/**
 * Interficie per gestionar les operacions de Partida
 */
public interface IPartidaDAO {

    /**
     * Llista els jugadors d'una partida
     */
    void llistarJugadorsPartida(SessionFactory sessionFactory, int idPartida);

    /**
     * Mostra l'estat actual de la partida
     */
    void mostrarPartida(SessionFactory sessionFactory, int idPartida);

    /**
     * Inicia una nova partida
     */
    Partida iniciarPartida(SessionFactory sessionFactory);

    /**
     * Comprova si hi ha un guanyador
     */
    void comprovarVictoria(SessionFactory sessionFactory, Partida partida);

    /**
     * Mostra una carta del coll (baralla)
     */
    TipusColl mostrarCarta(SessionFactory sessionFactory, int idPartida);
}

