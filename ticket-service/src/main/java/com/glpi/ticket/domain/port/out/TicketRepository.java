package com.glpi.ticket.domain.port.out;

import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.model.TicketStatus;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for Ticket aggregate.
 * Requirements: 22.3, 22.9
 */
public interface TicketRepository {

    Optional<Ticket> findById(String id);

    Ticket save(Ticket ticket);

    List<Ticket> findByEntityId(String entityId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findAllNotDeleted(int page, int size);

    long countAllNotDeleted();
}
