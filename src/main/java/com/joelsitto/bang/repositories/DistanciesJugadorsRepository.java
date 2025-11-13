package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.DistanciesJugadors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repository per accedir a les distàncies entre jugadors de la base de dades
@Repository
public interface DistanciesJugadorsRepository extends JpaRepository<DistanciesJugadors, Integer> {
}

