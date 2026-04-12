package com.glpi.sla.domain.port.out;

import com.glpi.sla.domain.model.Calendar;

import java.util.List;
import java.util.Optional;

/**
 * Driven port for Calendar persistence.
 * Requirements: 14.5
 */
public interface CalendarRepository {
    Optional<Calendar> findById(String id);
    Calendar save(Calendar calendar);
    void delete(String id);
    List<Calendar> findAll();
}
