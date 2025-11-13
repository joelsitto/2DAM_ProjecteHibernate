package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository per accedir a les partides de la base de dades
public interface PartidaRepository extends JpaRepository<Partida, Integer> { }
