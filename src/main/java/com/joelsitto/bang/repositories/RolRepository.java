package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Repository per accedir als rols de la base de dades
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Buscar un rol pel seu objectiu (ex: "Sheriff")
    Optional<Rol> findByObjectiu(String objectiu);
}

