package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.Carta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repository per accedir a les cartes de la base de dades
@Repository
public interface CartaRepository extends JpaRepository<Carta, Integer> { }