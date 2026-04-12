package com.glpi.sla.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoEscalationExecutionRepository
        extends MongoRepository<EscalationExecutionDocument, String> {

    boolean existsByTicketIdAndSlaIdAndLevelId(String ticketId, String slaId, String levelId);
}
