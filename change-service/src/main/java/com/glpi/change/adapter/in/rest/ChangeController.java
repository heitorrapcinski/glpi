package com.glpi.change.adapter.in.rest;

import com.glpi.change.domain.model.*;
import com.glpi.change.domain.port.in.*;
import com.glpi.change.domain.port.out.ChangeRepository;
import com.glpi.change.domain.service.AddSolutionService;
import com.glpi.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for change lifecycle and sub-resource operations.
 * Requirements: 11.1–11.10, 19.1, 19.6, 20.4
 */
@RestController
@RequestMapping("/changes")
@Tag(name = "Changes", description = "ITIL Change management")
public class ChangeController {

    private final CreateChangeUseCase createChangeUseCase;
    private final UpdateChangeUseCase updateChangeUseCase;
    private final CloseChangeUseCase closeChangeUseCase;
    private final LinkTicketToChangeUseCase linkTicketToChangeUseCase;
    private final ApproveChangeValidationUseCase approveChangeValidationUseCase;
    private final AddFollowupUseCase addFollowupUseCase;
    private final AddTaskUseCase addTaskUseCase;
    private final AddSolutionService addSolutionService;
    private final ChangeRepository changeRepository;

    public ChangeController(CreateChangeUseCase createChangeUseCase,
                            UpdateChangeUseCase updateChangeUseCase,
                            CloseChangeUseCase closeChangeUseCase,
                            LinkTicketToChangeUseCase linkTicketToChangeUseCase,
                            ApproveChangeValidationUseCase approveChangeValidationUseCase,
                            AddFollowupUseCase addFollowupUseCase,
                            AddTaskUseCase addTaskUseCase,
                            AddSolutionService addSolutionService,
                            ChangeRepository changeRepository) {
        this.createChangeUseCase = createChangeUseCase;
        this.updateChangeUseCase = updateChangeUseCase;
        this.closeChangeUseCase = closeChangeUseCase;
        this.linkTicketToChangeUseCase = linkTicketToChangeUseCase;
        this.approveChangeValidationUseCase = approveChangeValidationUseCase;
        this.addFollowupUseCase = addFollowupUseCase;
        this.addTaskUseCase = addTaskUseCase;
        this.addSolutionService = addSolutionService;
        this.changeRepository = changeRepository;
    }

    // ---- Change CRUD ----

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new change")
    public Change createChange(@RequestBody CreateChangeCommand command) {
        return createChangeUseCase.createChange(command);
    }

    @GetMapping
    @Operation(summary = "List all changes (paginated)")
    public PagedResponse<Change> listChanges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Change> changes = changeRepository.findAll(page, size);
        long total = changeRepository.countAll();
        return PagedResponse.of(changes, total, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a change by ID")
    public ResponseEntity<Change> getChange(@PathVariable String id) {
        return changeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a change")
    public Change updateChange(@PathVariable String id,
                               @RequestBody UpdateChangeCommand command) {
        return updateChangeUseCase.updateChange(new UpdateChangeCommand(
                id, command.title(), command.content(), command.status(),
                command.urgency(), command.impact(), command.priority(),
                command.planningDocuments()
        ));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a change")
    public Change patchChange(@PathVariable String id,
                              @RequestBody UpdateChangeCommand command) {
        return updateChangeUseCase.updateChange(new UpdateChangeCommand(
                id, command.title(), command.content(), command.status(),
                command.urgency(), command.impact(), command.priority(),
                command.planningDocuments()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a change")
    public void deleteChange(@PathVariable String id) {
        changeRepository.findById(id).orElseThrow(() -> new ChangeNotFoundException(id));
        changeRepository.delete(id);
    }

    // ---- Actors ----

    @GetMapping("/{id}/actors")
    @Operation(summary = "List actors on a change")
    public List<Actor> listActors(@PathVariable String id) {
        return changeRepository.findById(id)
                .map(Change::getActors)
                .orElseThrow(() -> new ChangeNotFoundException(id));
    }

    @PostMapping("/{id}/actors")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an actor to a change")
    public Change addActor(@PathVariable String id, @RequestBody Actor actor) {
        Change change = changeRepository.findById(id)
                .orElseThrow(() -> new ChangeNotFoundException(id));
        change.getActors().add(actor);
        return changeRepository.save(change);
    }

    @DeleteMapping("/{id}/actors/{actorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an actor from a change")
    public void removeActor(@PathVariable String id, @PathVariable String actorId) {
        Change change = changeRepository.findById(id)
                .orElseThrow(() -> new ChangeNotFoundException(id));
        change.getActors().removeIf(a -> a.getActorId().equals(actorId));
        changeRepository.save(change);
    }

    // ---- Followups ----

    @GetMapping("/{id}/followups")
    @Operation(summary = "List followups on a change")
    public List<Followup> listFollowups(@PathVariable String id) {
        return changeRepository.findById(id)
                .map(Change::getFollowups)
                .orElseThrow(() -> new ChangeNotFoundException(id));
    }

    @PostMapping("/{id}/followups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a followup to a change")
    public Change addFollowup(@PathVariable String id,
                              @RequestBody AddFollowupCommand command) {
        return addFollowupUseCase.addFollowup(new AddFollowupCommand(
                id, command.content(), command.authorId(), command.isPrivate(), command.source()
        ));
    }

    // ---- Tasks ----

    @GetMapping("/{id}/tasks")
    @Operation(summary = "List tasks on a change")
    public List<ChangeTask> listTasks(@PathVariable String id) {
        return changeRepository.findById(id)
                .map(Change::getTasks)
                .orElseThrow(() -> new ChangeNotFoundException(id));
    }

    @PostMapping("/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a task to a change")
    public Change addTask(@PathVariable String id,
                          @RequestBody AddTaskCommand command) {
        return addTaskUseCase.addTask(new AddTaskCommand(
                id, command.content(), command.assignedUserId(), command.status(), command.isPrivate()
        ));
    }

    // ---- Solutions ----

    @GetMapping("/{id}/solutions")
    @Operation(summary = "Get the solution on a change")
    public ResponseEntity<Solution> getSolution(@PathVariable String id) {
        Change change = changeRepository.findById(id)
                .orElseThrow(() -> new ChangeNotFoundException(id));
        if (change.getSolution() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(change.getSolution());
    }

    @PostMapping("/{id}/solutions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a solution to a change (transitions to SOLVED)")
    public Change addSolution(@PathVariable String id,
                              @RequestBody SolutionRequest request) {
        return addSolutionService.addSolution(id, request.content(), request.solutionType(), request.authorId());
    }

    // ---- Validations ----

    @GetMapping("/{id}/validations")
    @Operation(summary = "List validation steps on a change")
    public List<ValidationStep> listValidations(@PathVariable String id) {
        return changeRepository.findById(id)
                .map(Change::getValidationSteps)
                .orElseThrow(() -> new ChangeNotFoundException(id));
    }

    @PostMapping("/{id}/validations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a validation step to a change")
    public Change addValidation(@PathVariable String id,
                                @RequestBody ValidationStep validationStep) {
        Change change = changeRepository.findById(id)
                .orElseThrow(() -> new ChangeNotFoundException(id));
        if (validationStep.getId() == null || validationStep.getId().isBlank()) {
            validationStep.setId(java.util.UUID.randomUUID().toString());
        }
        if (validationStep.getStatus() == 0) {
            validationStep.setStatus(1); // WAITING by default
        }
        change.getValidationSteps().add(validationStep);
        return changeRepository.save(change);
    }

    @PutMapping("/{id}/validations/{validationId}")
    @Operation(summary = "Approve a validation step on a change")
    public Change approveValidation(@PathVariable String id,
                                    @PathVariable String validationId,
                                    @RequestBody ApproveChangeValidationCommand command) {
        return approveChangeValidationUseCase.approveValidation(
                new ApproveChangeValidationCommand(id, validationId, command.comment())
        );
    }

    // ---- Linked Tickets ----

    @GetMapping("/{id}/tickets")
    @Operation(summary = "List tickets linked to a change")
    public List<String> listLinkedTickets(@PathVariable String id) {
        return changeRepository.findById(id)
                .map(Change::getLinkedTicketIds)
                .orElseThrow(() -> new ChangeNotFoundException(id));
    }

    @PostMapping("/{id}/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Link a ticket to a change")
    public Change linkTicket(@PathVariable String id,
                             @RequestBody TicketLinkRequest request) {
        return linkTicketToChangeUseCase.linkTicket(new LinkTicketToChangeCommand(id, request.ticketId()));
    }

    // ---- Linked Problems ----

    @GetMapping("/{id}/problems")
    @Operation(summary = "List problems linked to a change")
    public List<String> listLinkedProblems(@PathVariable String id) {
        return changeRepository.findById(id)
                .map(Change::getLinkedProblemIds)
                .orElseThrow(() -> new ChangeNotFoundException(id));
    }

    @PostMapping("/{id}/problems")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Link a problem to a change")
    public Change linkProblem(@PathVariable String id,
                              @RequestBody ProblemLinkRequest request) {
        Change change = changeRepository.findById(id)
                .orElseThrow(() -> new ChangeNotFoundException(id));
        if (!change.getLinkedProblemIds().contains(request.problemId())) {
            change.getLinkedProblemIds().add(request.problemId());
            change.setUpdatedAt(java.time.Instant.now());
            change = changeRepository.save(change);
        }
        return change;
    }

    /** Request body for solution creation. */
    public record SolutionRequest(String content, String solutionType, String authorId) {}

    /** Request body for ticket linking. */
    public record TicketLinkRequest(String ticketId) {}

    /** Request body for problem linking. */
    public record ProblemLinkRequest(String problemId) {}
}
