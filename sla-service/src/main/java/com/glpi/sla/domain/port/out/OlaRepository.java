package com.glpi.sla.domain.port.out;

import com.glpi.sla.domain.model.Ola;

import java.util.List;
import java.util.Optional;

/**
 * Driven port for OLA persistence.
 * Requirements: 14.2, 22.7
 */
public interface OlaRepository {
    Optional<Ola> findById(String id);
    Ola save(Ola ola);
    void delete(String id);
    List<Ola> findAll();
}
