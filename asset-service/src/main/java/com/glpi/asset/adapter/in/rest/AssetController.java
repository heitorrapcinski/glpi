package com.glpi.asset.adapter.in.rest;

import com.glpi.asset.domain.model.*;
import com.glpi.asset.domain.port.in.*;
import com.glpi.asset.domain.port.out.AssetRepository;
import com.glpi.asset.domain.port.out.ItemSoftwareVersionRepository;
import com.glpi.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for asset CRUD and sub-resource operations.
 * Requirements: 12.1–12.12, 19.1, 19.6, 20.5
 */
@RestController
@RequestMapping("/assets")
@Tag(name = "Assets", description = "CMDB asset management")
public class AssetController {

    private final CreateAssetUseCase createAssetUseCase;
    private final UpdateAssetUseCase updateAssetUseCase;
    private final DeleteAssetUseCase deleteAssetUseCase;
    private final AssetRepository assetRepository;
    private final ItemSoftwareVersionRepository installationRepository;

    public AssetController(CreateAssetUseCase createAssetUseCase,
                           UpdateAssetUseCase updateAssetUseCase,
                           DeleteAssetUseCase deleteAssetUseCase,
                           AssetRepository assetRepository,
                           ItemSoftwareVersionRepository installationRepository) {
        this.createAssetUseCase = createAssetUseCase;
        this.updateAssetUseCase = updateAssetUseCase;
        this.deleteAssetUseCase = deleteAssetUseCase;
        this.assetRepository = assetRepository;
        this.installationRepository = installationRepository;
    }

    // ---- Asset CRUD by type ----

    @GetMapping("/{type}")
    @Operation(summary = "List assets by type (paginated)")
    public PagedResponse<Asset> listByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AssetType assetType = AssetType.valueOf(type);
        List<Asset> assets = assetRepository.findByType(assetType, page, size);
        long total = assetRepository.countByType(assetType);
        return PagedResponse.of(assets, total, page, size);
    }

    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new asset of the given type")
    public Asset createAsset(@PathVariable String type,
                             @RequestBody CreateAssetCommand command) {
        AssetType assetType = AssetType.valueOf(type);
        return createAssetUseCase.createAsset(new CreateAssetCommand(
                assetType, command.name(), command.entityId(),
                command.serial(), command.otherSerial(), command.stateId(),
                command.locationId(), command.userId(), command.groupId(),
                command.manufacturerId(), command.modelId(),
                command.infocom(), command.computerDetails()
        ));
    }

    @GetMapping("/{type}/{id}")
    @Operation(summary = "Get an asset by type and ID")
    public ResponseEntity<Asset> getAsset(@PathVariable String type, @PathVariable String id) {
        return assetRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{type}/{id}")
    @Operation(summary = "Update an asset")
    public Asset updateAsset(@PathVariable String type, @PathVariable String id,
                             @RequestBody UpdateAssetCommand command) {
        return updateAssetUseCase.updateAsset(new UpdateAssetCommand(
                id, command.name(), command.serial(), command.otherSerial(),
                command.stateId(), command.locationId(), command.userId(),
                command.groupId(), command.manufacturerId(), command.modelId(),
                command.infocom(), command.computerDetails()
        ));
    }

    @DeleteMapping("/{type}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft-delete an asset")
    public void deleteAsset(@PathVariable String type, @PathVariable String id) {
        deleteAssetUseCase.deleteAsset(id);
    }

    // ---- Network Ports ----

    @GetMapping("/{type}/{id}/networkports")
    @Operation(summary = "List network ports on an asset")
    public List<NetworkPort> listNetworkPorts(@PathVariable String type, @PathVariable String id) {
        return assetRepository.findById(id)
                .map(Asset::getNetworkPorts)
                .orElseThrow(() -> new AssetNotFoundException(id));
    }

    @PostMapping("/{type}/{id}/networkports")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a network port to an asset")
    public Asset addNetworkPort(@PathVariable String type, @PathVariable String id,
                                @RequestBody NetworkPort port) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));
        if (port.getId() == null || port.getId().isBlank()) {
            port.setId(UUID.randomUUID().toString());
        }
        asset.getNetworkPorts().add(port);
        asset.setUpdatedAt(java.time.Instant.now());
        return assetRepository.save(asset);
    }

    // ---- Computer-specific sub-resources ----

    @GetMapping("/computers/{id}/software")
    @Operation(summary = "List software installations on a computer")
    public List<ItemSoftwareVersion> listComputerSoftware(@PathVariable String id) {
        assetRepository.findById(id).orElseThrow(() -> new AssetNotFoundException(id));
        return installationRepository.findByAssetId(id);
    }

    @GetMapping("/computers/{id}/devices")
    @Operation(summary = "List hardware devices on a computer")
    public ResponseEntity<Object> listComputerDevices(@PathVariable String id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));
        if (asset.getComputerDetails() != null) {
            return ResponseEntity.ok(asset.getComputerDetails().getDevices());
        }
        return ResponseEntity.ok(List.of());
    }

    // ---- Cross-service ticket link (returns contract IDs as placeholder) ----

    @GetMapping("/{type}/{id}/tickets")
    @Operation(summary = "List tickets linked to an asset (cross-service)")
    public ResponseEntity<List<String>> listLinkedTickets(@PathVariable String type, @PathVariable String id) {
        // Cross-service query placeholder — returns empty list
        // In production, this would call Ticket Service via HTTP
        assetRepository.findById(id).orElseThrow(() -> new AssetNotFoundException(id));
        return ResponseEntity.ok(List.of());
    }
}
