package com.glpi.sla.domain.port.out;

import com.glpi.sla.domain.model.Sla;

import java.util.List;
import java.util.Optional;

/**
 * Driven port for SLA persistence.
 * Requirements: 14.1, 22.7
 */
public interface SlaRepository {
    Optional<Sla> findById(String id);
    Sla save(Sla sla);
    void delete(String id);
    List<Sla> findAll();
}
