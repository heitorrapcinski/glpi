package com.glpi.problem.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.problem.domain.model.*;
import com.glpi.problem.domain.port.in.*;
import com.glpi.problem.domain.port.out.ProblemRepository;
import com.glpi.problem.domain.service.AddSolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for problem lifecycle and sub-resource operations.
 * Requirements: 10.1–10.9, 19.1, 19.6, 20.3
 */
@RestController
@RequestMapping("/problems")
@Tag(name = "Problems", description = "ITIL Problem management")
public class ProblemController {

    private final CreateProblemUseCase createProblemUseCase;
    private final UpdateProblemUseCase updateProblemUseCase;
    private final LinkTicketToProblemUseCase linkTicketToProblemUseCase;
    private final SolveProblemUseCase solveProblemUseCase;
    private final CloseProblemUseCase closeProblemUseCase;
    private final AddFollowupUseCase addFollowupUseCase;
    private final AddTaskUseCase addTaskUseCase;
    private final AddSolutionService addSolutionService;
    private final ProblemRepository problemRepository;

    public ProblemController(CreateProblemUseCase createProblemUseCase,
                             UpdateProblemUseCase updateProblemUseCase,
                             LinkTicketToProblemUseCase linkTicketToProblemUseCase,
                             SolveProblemUseCase solveProblemUseCase,
                             CloseProblemUseCase closeProblemUseCase,
                             AddFollowupUseCase addFollowupUseCase,
                             AddTaskUseCase addTaskUseCase,
                             AddSolutionService addSolutionService,
                             ProblemRepository problemRepository) {
        this.createProblemUseCase = createProblemUseCase;
        this.updateProblemUseCase = updateProblemUseCase;
        this.linkTicketToProblemUseCase = linkTicketToProblemUseCase;
        this.solveProblemUseCase = solveProblemUseCase;
        this.closeProblemUseCase = closeProblemUseCase;
        this.addFollowupUseCase = addFollowupUseCase;
        this.addTaskUseCase = addTaskUseCase;
        this.addSolutionService = addSolutionService;
        this.problemRepository = problemRepository;
    }

    // ---- Problem CRUD ----

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new problem")
    public Problem createProblem(@Valid @RequestBody CreateProblemCommand command) {
        return createProblemUseCase.createProblem(command);
    }

    @GetMapping
    @Operation(summary = "List all problems (paginated)")
    public PagedResponse<Problem> listProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<Problem> problems = problemRepository.findAll(page, clampedSize);
        long total = problemRepository.countAll();
        return PagedResponse.of(problems, total, page, clampedSize);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a problem by ID")
    public ResponseEntity<Problem> getProblem(@PathVariable String id) {
        return problemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a problem")
    public Problem updateProblem(@PathVariable String id,
                                 @RequestBody UpdateProblemCommand command) {
        UpdateProblemCommand cmd = new UpdateProblemCommand(
                id, command.title(), command.content(), command.status(),
                command.urgency(), command.impact(), command.priority(),
                command.impactContent(), command.causeContent(), command.symptomContent()
        );
        return updateProblemUseCase.updateProblem(cmd);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a problem")
    public Problem patchProblem(@PathVariable String id,
                                @RequestBody UpdateProblemCommand command) {
        UpdateProblemCommand cmd = new UpdateProblemCommand(
                id, command.title(), command.content(), command.status(),
                command.urgency(), command.impact(), command.priority(),
                command.impactContent(), command.causeContent(), command.symptomContent()
        );
        return updateProblemUseCase.updateProblem(cmd);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a problem")
    public void deleteProblem(@PathVariable String id) {
        problemRepository.findById(id).orElseThrow(() -> new ProblemNotFoundException(id));
        problemRepository.delete(id);
    }

    // ---- Actors ----

    @GetMapping("/{id}/actors")
    @Operation(summary = "List actors on a problem")
    public List<Actor> listActors(@PathVariable String id) {
        return problemRepository.findById(id)
                .map(Problem::getActors)
                .orElseThrow(() -> new ProblemNotFoundException(id));
    }

    @PostMapping("/{id}/actors")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an actor to a problem")
    public Problem addActor(@PathVariable String id, @RequestBody Actor actor) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException(id));
        problem.getActors().add(actor);
        return problemRepository.save(problem);
    }

    @DeleteMapping("/{id}/actors/{actorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an actor from a problem")
    public void removeActor(@PathVariable String id, @PathVariable String actorId) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException(id));
        problem.getActors().removeIf(a -> a.getActorId().equals(actorId));
        problemRepository.save(problem);
    }

    // ---- Followups ----

    @GetMapping("/{id}/followups")
    @Operation(summary = "List followups on a problem")
    public List<Followup> listFollowups(@PathVariable String id) {
        return problemRepository.findById(id)
                .map(Problem::getFollowups)
                .orElseThrow(() -> new ProblemNotFoundException(id));
    }

    @PostMapping("/{id}/followups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a followup to a problem")
    public Problem addFollowup(@PathVariable String id,
                               @RequestBody AddFollowupCommand command) {
        AddFollowupCommand cmd = new AddFollowupCommand(
                id, command.content(), command.authorId(), command.isPrivate(), command.source()
        );
        return addFollowupUseCase.addFollowup(cmd);
    }

    // ---- Tasks ----

    @GetMapping("/{id}/tasks")
    @Operation(summary = "List tasks on a problem")
    public List<ProblemTask> listTasks(@PathVariable String id) {
        return problemRepository.findById(id)
                .map(Problem::getTasks)
                .orElseThrow(() -> new ProblemNotFoundException(id));
    }

    @PostMapping("/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a task to a problem")
    public Problem addTask(@PathVariable String id,
                           @RequestBody AddTaskCommand command) {
        AddTaskCommand cmd = new AddTaskCommand(
                id, command.content(), command.assignedUserId(), command.status(), command.isPrivate()
        );
        return addTaskUseCase.addTask(cmd);
    }

    // ---- Solutions ----

    @GetMapping("/{id}/solutions")
    @Operation(summary = "Get the solution on a problem")
    public ResponseEntity<Solution> getSolution(@PathVariable String id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException(id));
        if (problem.getSolution() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(problem.getSolution());
    }

    @PostMapping("/{id}/solutions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a solution to a problem (transitions to SOLVED)")
    public Problem addSolution(@PathVariable String id,
                               @RequestBody SolutionRequest request) {
        return addSolutionService.addSolution(id, request.content(), request.solutionType(), request.authorId());
    }

    // ---- Linked Tickets ----

    @GetMapping("/{id}/tickets")
    @Operation(summary = "List tickets linked to a problem")
    public List<String> listLinkedTickets(@PathVariable String id) {
        return problemRepository.findById(id)
                .map(Problem::getLinkedTicketIds)
                .orElseThrow(() -> new ProblemNotFoundException(id));
    }

    @PostMapping("/{id}/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Link a ticket to a problem")
    public Problem linkTicket(@PathVariable String id,
                              @RequestBody TicketLinkRequest request) {
        return linkTicketToProblemUseCase.linkTicket(
                new LinkTicketToProblemCommand(id, request.ticketId()));
    }

    @DeleteMapping("/{id}/tickets/{ticketId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unlink a ticket from a problem")
    public void unlinkTicket(@PathVariable String id, @PathVariable String ticketId) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException(id));
        problem.getLinkedTicketIds().remove(ticketId);
        problemRepository.save(problem);
    }

    /** Request body for solution creation. */
    public record SolutionRequest(String content, String solutionType, String authorId) {}

    /** Request body for ticket linking. */
    public record TicketLinkRequest(String ticketId) {}

    // ---- Bulk Operations ----

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Bulk create problems (max 100 items)")
    public List<Problem> bulkCreateProblems(@Valid @RequestBody List<CreateProblemCommand> commands) {
        if (commands.size() > 100) {
            throw new IllegalArgumentException("Bulk operations are limited to 100 items");
        }
        return commands.stream()
                .map(createProblemUseCase::createProblem)
                .toList();
    }
}
