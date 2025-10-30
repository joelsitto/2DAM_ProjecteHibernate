package com.joelsitto.bang.dao;

import com.joelsitto.bang.model.Jugador;
import com.joelsitto.bang.model.Partida;
import com.joelsitto.bang.model.Rol;
import org.hibernate.SessionFactory;
import java.util.List;

/**
 * Interficie per gestionar les operacions de Partida
 */
public interface IPartidaDAO {

    /**
     * Llista els jugadors d'una partida
     */
    List<Jugador> llistarJugadorsPartida(SessionFactory sessionFactory, int idPartida);

    /**
     * Mostra l'estat actual de la partida
     */
    void mostrarPartida(SessionFactory sessionFactory, int idPartida);

    /**
     * Inicia una nova partida
     */
    Partida iniciarPartida(SessionFactory sessionFactory, List<String> nomsJugadors, List<Rol> roles);

    /**
     * Comprova si hi ha un guanyador
     */
    String comprovarVictoria(SessionFactory sessionFactory, int idPartida);

    /**
     * Mostra una carta del coll (baralla)
     */
    String mostrarCarta(SessionFactory sessionFactory, int idPartida);
}

