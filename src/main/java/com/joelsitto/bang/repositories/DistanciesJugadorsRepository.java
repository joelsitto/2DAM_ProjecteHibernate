package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.DistanciesJugadors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistanciesJugadorsRepository extends JpaRepository<DistanciesJugadors, Integer> {
}

