package com.glpi.sla.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.sla.domain.model.Sla;
import com.glpi.sla.domain.model.SlaAction;
import com.glpi.sla.domain.model.SlaLevel;
import com.glpi.sla.domain.model.SlaNotFoundException;
import com.glpi.sla.domain.model.SlaType;
import com.glpi.sla.domain.port.in.DeadlineComputationPort;
import com.glpi.sla.domain.port.out.SlaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for SLA CRUD and deadline computation.
 * Requirements: 14.4, 14.8, 19.1, 19.6
 */
@RestController
@RequestMapping("/slas")
@Tag(name = "SLAs", description = "Service Level Agreement management")
public class SlaController {

    private final SlaRepository slaRepository;
    private final DeadlineComputationPort deadlineComputationPort;

    public SlaController(SlaRepository slaRepository, DeadlineComputationPort deadlineComputationPort) {
        this.slaRepository = slaRepository;
        this.deadlineComputationPort = deadlineComputationPort;
    }

    @GetMapping
    @Operation(summary = "List all SLAs (paginated)")
    public PagedResponse<Sla> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<Sla> all = slaRepository.findAll();
        int from = Math.min(page * clampedSize, all.size());
        int to = Math.min(from + clampedSize, all.size());
        return PagedResponse.of(all.subList(from, to), all.size(), page, clampedSize);
    }

    @PostMapping
    @Operation(summary = "Create an SLA")
    public ResponseEntity<Sla> create(@RequestBody Map<String, Object> body) {
        Sla sla = fromBody(UUID.randomUUID().toString(), body);
        sla.setCreatedAt(Instant.now());
        sla.setUpdatedAt(Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(slaRepository.save(sla));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an SLA by ID")
    public Sla getById(@PathVariable String id) {
        return slaRepository.findById(id).orElseThrow(() -> new SlaNotFoundException(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an SLA")
    public Sla update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Sla existing = slaRepository.findById(id).orElseThrow(() -> new SlaNotFoundException(id));
        Sla updated = fromBody(id, body);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        return slaRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an SLA")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        slaRepository.findById(id).orElseThrow(() -> new SlaNotFoundException(id));
        slaRepository.delete(id);
    }

    @PostMapping("/compute-deadline")
    @Operation(summary = "Compute SLA deadline accounting for business hours")
    public Map<String, Object> computeDeadline(@RequestBody Map<String, Object> body) {
        String startDateStr = (String) body.get("startDate");
        long durationSeconds = ((Number) body.get("durationSeconds")).longValue();
        String calendarId = (String) body.get("calendarId");

        Instant start = Instant.parse(startDateStr);
        Instant deadline = deadlineComputationPort.computeDeadline(start, durationSeconds, calendarId);

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDateStr);
        result.put("durationSeconds", durationSeconds);
        result.put("calendarId", calendarId);
        result.put("deadline", deadline.toString());
        return result;
    }

    @SuppressWarnings("unchecked")
    private Sla fromBody(String id, Map<String, Object> body) {
        Sla sla = new Sla();
        sla.setId(id);
        sla.setName((String) body.get("name"));
        sla.setEntityId((String) body.getOrDefault("entityId", "0"));
        sla.setType(SlaType.fromValue(((Number) body.getOrDefault("type", 2)).intValue()));
        sla.setDurationSeconds(((Number) body.getOrDefault("durationSeconds", 0)).longValue());
        sla.setCalendarId((String) body.get("calendarId"));

        List<SlaLevel> levels = new ArrayList<>();
        if (body.get("levels") instanceof List<?> rawLevels) {
            for (Object l : rawLevels) {
                Map<String, Object> lm = (Map<String, Object>) l;
                List<SlaAction> actions = new ArrayList<>();
                if (lm.get("actions") instanceof List<?> rawActions) {
                    for (Object a : rawActions) {
                        Map<String, Object> am = (Map<String, Object>) a;
                        actions.add(new SlaAction(
                                (String) am.get("actionType"),
                                (Map<String, Object>) am.getOrDefault("parameters", new HashMap<String, Object>())
                        ));
                    }
                }
                levels.add(new SlaLevel(
                        (String) lm.getOrDefault("id", UUID.randomUUID().toString()),
                        (String) lm.get("name"),
                        ((Number) lm.getOrDefault("executionDelaySeconds", 0)).longValue(),
                        actions
                ));
            }
        }
        sla.setLevels(levels);
        return sla;
    }
}
