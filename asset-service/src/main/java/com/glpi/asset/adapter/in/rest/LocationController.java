package com.glpi.asset.adapter.in.rest;

import com.glpi.asset.domain.model.Location;
import com.glpi.asset.domain.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for hierarchical location management.
 * Requirements: 12.4
 */
@RestController
@RequestMapping("/assets/locations")
@Tag(name = "Locations", description = "Hierarchical location tree")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "List all locations")
    public List<Location> listLocations() {
        return locationService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new location")
    public Location createLocation(@RequestBody LocationRequest request) {
        return locationService.createLocation(request.name(), request.parentId());
    }

    public record LocationRequest(String name, String parentId) {}
}
