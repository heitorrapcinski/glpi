package com.glpi.ticket.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.port.in.*;
import com.glpi.ticket.domain.port.out.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for ticket lifecycle operations.
 * Requirements: 5.1, 5.3, 5.4, 5.11, 19.1, 19.2, 19.6
 */
@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "ITIL Incident and Service Request management")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final UpdateTicketUseCase updateTicketUseCase;
    private final AssignTicketUseCase assignTicketUseCase;
    private final DeleteTicketUseCase deleteTicketUseCase;
    private final TicketRepository ticketRepository;

    public TicketController(CreateTicketUseCase createTicketUseCase,
                            UpdateTicketUseCase updateTicketUseCase,
                            AssignTicketUseCase assignTicketUseCase,
                            DeleteTicketUseCase deleteTicketUseCase,
                            TicketRepository ticketRepository) {
        this.createTicketUseCase = createTicketUseCase;
        this.updateTicketUseCase = updateTicketUseCase;
        this.assignTicketUseCase = assignTicketUseCase;
        this.deleteTicketUseCase = deleteTicketUseCase;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new ticket")
    public Ticket createTicket(@RequestBody CreateTicketCommand command) {
        return createTicketUseCase.createTicket(command);
    }

    @GetMapping
    @Operation(summary = "List all non-deleted tickets (paginated)")
    public PagedResponse<Ticket> listTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Ticket> tickets = ticketRepository.findAllNotDeleted(page, size);
        long total = ticketRepository.countAllNotDeleted();
        return PagedResponse.of(tickets, total, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a ticket by ID")
    public ResponseEntity<Ticket> getTicket(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a ticket")
    public Ticket updateTicket(@PathVariable String id,
                               @RequestBody UpdateTicketCommand command,
                               @RequestHeader(value = "X-User-Rights", defaultValue = "0") int userRights) {
        // Rebuild command with path id and user rights from header
        UpdateTicketCommand cmd = new UpdateTicketCommand(
                id, command.title(), command.content(), command.type(), command.status(),
                command.urgency(), command.impact(), command.priority(), command.categoryId(),
                userRights
        );
        return updateTicketUseCase.updateTicket(cmd);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign a ticket to a user or group")
    public Ticket assignTicket(@PathVariable String id,
                               @RequestBody AssignTicketCommand command,
                               @RequestHeader(value = "X-User-Rights", defaultValue = "0") int userRights) {
        AssignTicketCommand cmd = new AssignTicketCommand(
                id, command.assigneeId(), command.assigneeKind(), userRights
        );
        return assignTicketUseCase.assignTicket(cmd);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft-delete a ticket")
    public void deleteTicket(@PathVariable String id) {
        deleteTicketUseCase.deleteTicket(id);
    }
}
