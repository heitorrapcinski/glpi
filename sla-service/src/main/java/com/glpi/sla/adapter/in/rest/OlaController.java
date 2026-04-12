package com.glpi.sla.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.sla.domain.model.Ola;
import com.glpi.sla.domain.model.OlaNotFoundException;
import com.glpi.sla.domain.model.SlaAction;
import com.glpi.sla.domain.model.SlaLevel;
import com.glpi.sla.domain.model.SlaType;
import com.glpi.sla.domain.port.out.OlaRepository;
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
 * REST controller for OLA CRUD.
 * Requirements: 14.4, 19.1, 19.6
 */
@RestController
@RequestMapping("/olas")
@Tag(name = "OLAs", description = "Operational Level Agreement management")
public class OlaController {

    private final OlaRepository olaRepository;

    public OlaController(OlaRepository olaRepository) {
        this.olaRepository = olaRepository;
    }

    @GetMapping
    @Operation(summary = "List all OLAs (paginated)")
    public PagedResponse<Ola> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<Ola> all = olaRepository.findAll();
        int from = Math.min(page * clampedSize, all.size());
        int to = Math.min(from + clampedSize, all.size());
        return PagedResponse.of(all.subList(from, to), all.size(), page, clampedSize);
    }

    @PostMapping
    @Operation(summary = "Create an OLA")
    public ResponseEntity<Ola> create(@RequestBody Map<String, Object> body) {
        Ola ola = fromBody(UUID.randomUUID().toString(), body);
        ola.setCreatedAt(Instant.now());
        ola.setUpdatedAt(Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(olaRepository.save(ola));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an OLA by ID")
    public Ola getById(@PathVariable String id) {
        return olaRepository.findById(id).orElseThrow(() -> new OlaNotFoundException(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an OLA")
    public Ola update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Ola existing = olaRepository.findById(id).orElseThrow(() -> new OlaNotFoundException(id));
        Ola updated = fromBody(id, body);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        return olaRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an OLA")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        olaRepository.findById(id).orElseThrow(() -> new OlaNotFoundException(id));
        olaRepository.delete(id);
    }

    @SuppressWarnings("unchecked")
    private Ola fromBody(String id, Map<String, Object> body) {
        Ola ola = new Ola();
        ola.setId(id);
        ola.setName((String) body.get("name"));
        ola.setEntityId((String) body.getOrDefault("entityId", "0"));
        ola.setType(SlaType.fromValue(((Number) body.getOrDefault("type", 2)).intValue()));
        ola.setDurationSeconds(((Number) body.getOrDefault("durationSeconds", 0)).longValue());
        ola.setCalendarId((String) body.get("calendarId"));

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
        ola.setLevels(levels);
        return ola;
    }
}
