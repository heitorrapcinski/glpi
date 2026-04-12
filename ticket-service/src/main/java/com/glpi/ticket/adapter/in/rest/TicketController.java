package com.glpi.ticket.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.*;
import com.glpi.ticket.domain.port.out.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for ticket lifecycle and sub-resource operations.
 * Requirements: 5.1, 5.3, 5.4, 5.11, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 8.4,
 *               19.1, 19.2, 19.6, 19.7, 20.1, 20.2
 */
@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "ITIL Incident and Service Request management")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final UpdateTicketUseCase updateTicketUseCase;
    private final AssignTicketUseCase assignTicketUseCase;
    private final DeleteTicketUseCase deleteTicketUseCase;
    private final AddFollowupUseCase addFollowupUseCase;
    private final AddTaskUseCase addTaskUseCase;
    private final AddSolutionUseCase addSolutionUseCase;
    private final RejectSolutionUseCase rejectSolutionUseCase;
    private final RequestValidationUseCase requestValidationUseCase;
    private final ApproveValidationUseCase approveValidationUseCase;
    private final RefuseValidationUseCase refuseValidationUseCase;
    private final TicketRepository ticketRepository;

    public TicketController(CreateTicketUseCase createTicketUseCase,
                            UpdateTicketUseCase updateTicketUseCase,
                            AssignTicketUseCase assignTicketUseCase,
                            DeleteTicketUseCase deleteTicketUseCase,
                            AddFollowupUseCase addFollowupUseCase,
                            AddTaskUseCase addTaskUseCase,
                            AddSolutionUseCase addSolutionUseCase,
                            RejectSolutionUseCase rejectSolutionUseCase,
                            RequestValidationUseCase requestValidationUseCase,
                            ApproveValidationUseCase approveValidationUseCase,
                            RefuseValidationUseCase refuseValidationUseCase,
                            TicketRepository ticketRepository) {
        this.createTicketUseCase = createTicketUseCase;
        this.updateTicketUseCase = updateTicketUseCase;
        this.assignTicketUseCase = assignTicketUseCase;
        this.deleteTicketUseCase = deleteTicketUseCase;
        this.addFollowupUseCase = addFollowupUseCase;
        this.addTaskUseCase = addTaskUseCase;
        this.addSolutionUseCase = addSolutionUseCase;
        this.rejectSolutionUseCase = rejectSolutionUseCase;
        this.requestValidationUseCase = requestValidationUseCase;
        this.approveValidationUseCase = approveValidationUseCase;
        this.refuseValidationUseCase = refuseValidationUseCase;
        this.ticketRepository = ticketRepository;
    }

    // ---- Ticket CRUD ----

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
        UpdateTicketCommand cmd = new UpdateTicketCommand(
                id, command.title(), command.content(), command.type(), command.status(),
                command.urgency(), command.impact(), command.priority(), command.categoryId(),
                userRights
        );
        return updateTicketUseCase.updateTicket(cmd);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a ticket")
    public Ticket patchTicket(@PathVariable String id,
                              @RequestBody UpdateTicketCommand command,
                              @RequestHeader(value = "X-User-Rights", defaultValue = "0") int userRights) {
        UpdateTicketCommand cmd = new UpdateTicketCommand(
                id, command.title(), command.content(), command.type(), command.status(),
                command.urgency(), command.impact(), command.priority(), command.categoryId(),
                userRights
        );
        return updateTicketUseCase.updateTicket(cmd);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft-delete a ticket")
    public void deleteTicket(@PathVariable String id) {
        deleteTicketUseCase.deleteTicket(id);
    }

    // ---- Actors ----

    @GetMapping("/{id}/actors")
    @Operation(summary = "List actors on a ticket")
    public List<Actor> listActors(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(Ticket::getActors)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @PostMapping("/{id}/actors")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an actor to a ticket")
    public Ticket addActor(@PathVariable String id,
                           @RequestBody Actor actor,
                           @RequestHeader(value = "X-User-Rights", defaultValue = "0") int userRights) {
        AssignTicketCommand cmd = new AssignTicketCommand(
                id, actor.getActorId(), actor.getActorKind(), userRights
        );
        return assignTicketUseCase.assignTicket(cmd);
    }

    @DeleteMapping("/{id}/actors/{actorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an actor from a ticket")
    public void removeActor(@PathVariable String id, @PathVariable String actorId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        ticket.getActors().removeIf(a -> a.getActorId().equals(actorId));
        ticketRepository.save(ticket);
    }

    // ---- Followups ----

    @GetMapping("/{id}/followups")
    @Operation(summary = "List followups on a ticket")
    public List<Followup> listFollowups(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(Ticket::getFollowups)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @PostMapping("/{id}/followups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a followup to a ticket")
    public Ticket addFollowup(@PathVariable String id,
                              @RequestBody AddFollowupCommand command) {
        AddFollowupCommand cmd = new AddFollowupCommand(
                id, command.content(), command.authorId(), command.isPrivate(), command.source()
        );
        return addFollowupUseCase.addFollowup(cmd);
    }

    // ---- Tasks ----

    @GetMapping("/{id}/tasks")
    @Operation(summary = "List tasks on a ticket")
    public List<TicketTask> listTasks(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(Ticket::getTasks)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @PostMapping("/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a task to a ticket")
    public Ticket addTask(@PathVariable String id,
                          @RequestBody AddTaskCommand command) {
        AddTaskCommand cmd = new AddTaskCommand(
                id, command.content(), command.assignedUserId(),
                command.plannedStart(), command.plannedEnd(),
                command.duration(), command.status(), command.isPrivate()
        );
        return addTaskUseCase.addTask(cmd);
    }

    @PutMapping("/{id}/tasks/{taskId}")
    @Operation(summary = "Update a task on a ticket")
    public Ticket updateTask(@PathVariable String id,
                             @PathVariable String taskId,
                             @RequestBody AddTaskCommand command) {
        // For simplicity, update by removing old and adding new
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        ticket.getTasks().removeIf(t -> t.getId().equals(taskId));
        ticketRepository.save(ticket);

        AddTaskCommand cmd = new AddTaskCommand(
                id, command.content(), command.assignedUserId(),
                command.plannedStart(), command.plannedEnd(),
                command.duration(), command.status(), command.isPrivate()
        );
        return addTaskUseCase.addTask(cmd);
    }

    // ---- Solutions ----

    @GetMapping("/{id}/solutions")
    @Operation(summary = "Get the solution on a ticket")
    public ResponseEntity<Solution> getSolution(@PathVariable String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        if (ticket.getSolution() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket.getSolution());
    }

    @PostMapping("/{id}/solutions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a solution to a ticket (transitions to SOLVED)")
    public Ticket addSolution(@PathVariable String id,
                              @RequestBody AddSolutionCommand command) {
        AddSolutionCommand cmd = new AddSolutionCommand(
                id, command.content(), command.solutionType(), command.authorId()
        );
        return addSolutionUseCase.addSolution(cmd);
    }

    @PostMapping("/{id}/solutions/reject")
    @Operation(summary = "Reject the current solution (reopens ticket to ASSIGNED)")
    public Ticket rejectSolution(@PathVariable String id,
                                 @RequestBody RejectSolutionCommand command) {
        RejectSolutionCommand cmd = new RejectSolutionCommand(
                id, command.rejectorId(), command.reason()
        );
        return rejectSolutionUseCase.rejectSolution(cmd);
    }

    // ---- Validations ----

    @GetMapping("/{id}/validations")
    @Operation(summary = "List validations on a ticket")
    public List<Validation> listValidations(@PathVariable String id) {
        return ticketRepository.findById(id)
                .map(Ticket::getValidations)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @PostMapping("/{id}/validations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Request a validation on a ticket")
    public Ticket requestValidation(@PathVariable String id,
                                    @RequestBody RequestValidationCommand command) {
        RequestValidationCommand cmd = new RequestValidationCommand(
                id, command.validatorId(), command.validatorKind()
        );
        return requestValidationUseCase.requestValidation(cmd);
    }

    @PutMapping("/{id}/validations/{validationId}")
    @Operation(summary = "Approve or refuse a validation")
    public Ticket updateValidation(@PathVariable String id,
                                   @PathVariable String validationId,
                                   @RequestBody ValidationUpdateRequest request) {
        if ("ACCEPTED".equalsIgnoreCase(request.action())) {
            return approveValidationUseCase.approveValidation(
                    new ApproveValidationCommand(id, validationId, request.comment()));
        } else if ("REFUSED".equalsIgnoreCase(request.action())) {
            return refuseValidationUseCase.refuseValidation(
                    new RefuseValidationCommand(id, validationId, request.comment()));
        }
        throw new IllegalArgumentException("action must be ACCEPTED or REFUSED");
    }

    /** Simple request body for validation approve/refuse. */
    public record ValidationUpdateRequest(String action, String comment) {}
}
