package com.glpi.asset.adapter.in.rest;

import com.glpi.asset.domain.model.AssetState;
import com.glpi.asset.domain.service.AssetStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for asset state management.
 * Requirements: 12.3
 */
@RestController
@RequestMapping("/assets/states")
@Tag(name = "Asset States", description = "Configurable asset lifecycle states")
public class AssetStateController {

    private final AssetStateService assetStateService;

    public AssetStateController(AssetStateService assetStateService) {
        this.assetStateService = assetStateService;
    }

    @GetMapping
    @Operation(summary = "List all asset states")
    public List<AssetState> listStates() {
        return assetStateService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new asset state")
    public AssetState createState(@RequestBody StateRequest request) {
        return assetStateService.createState(request.name());
    }

    public record StateRequest(String name) {}
}
