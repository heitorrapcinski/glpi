package com.glpi.ticket.adapter.out.persistence;

import com.glpi.ticket.domain.model.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for TicketDocument.
 * Requirements: 22.3, 22.9
 */
public interface MongoTicketRepository extends MongoRepository<TicketDocument, String> {

    List<TicketDocument> findByEntityIdAndIsDeletedFalse(String entityId);

    List<TicketDocument> findByStatusAndIsDeletedFalse(TicketStatus status);

    List<TicketDocument> findByIsDeletedFalse(Pageable pageable);

    long countByIsDeletedFalse();
}
