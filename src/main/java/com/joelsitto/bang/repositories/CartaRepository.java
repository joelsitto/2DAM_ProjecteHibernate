package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.Carta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartaRepository extends JpaRepository<Carta, Integer> { }