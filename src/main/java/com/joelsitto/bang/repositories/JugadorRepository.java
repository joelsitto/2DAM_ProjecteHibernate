package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Repository per accedir als jugadors de la base de dades
public interface JugadorRepository extends JpaRepository<Jugador, Integer> {
    // Buscar jugadors per ID de partida
    List<Jugador> findByPartidaId(int idPartida);
}