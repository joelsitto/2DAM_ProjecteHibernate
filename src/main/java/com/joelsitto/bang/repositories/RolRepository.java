package com.joelsitto.bang.repositories;

import com.joelsitto.bang.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository per gestionar els Rols
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Cerca un rol pel seu objectiu
     * @param objectiu Descripció de l'objectiu del rol
     * @return Optional amb el rol si existeix
     */
    Optional<Rol> findByObjectiu(String objectiu);
}

