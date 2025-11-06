package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {
    List<Jugador> findByPartidaId(int idPartida);
}