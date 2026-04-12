package com.glpi.asset.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Asset aggregate root — polymorphic CMDB configuration item.
 * The {@code assetType} field acts as a discriminator for type-specific behavior.
 * Requirements: 12.1, 12.2, 12.9, 12.10, 12.11, 12.12, 22.6
 */
public class Asset {

    private String id;
    private AssetType assetType;
    private String name;
    private String entityId;
    private String serial;
    private String otherSerial;
    private String stateId;
    private String locationId;
    private String userId;
    private String groupId;
    private String manufacturerId;
    private String modelId;
    private boolean isDeleted;
    private List<NetworkPort> networkPorts;
    private Infocom infocom;
    private List<String> contractIds;
    private ComputerDetails computerDetails;
    private Instant createdAt;
    private Instant updatedAt;

    public Asset() {
        this.networkPorts = new ArrayList<>();
        this.contractIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public String getOtherSerial() { return otherSerial; }
    public void setOtherSerial(String otherSerial) { this.otherSerial = otherSerial; }

    public String getStateId() { return stateId; }
    public void setStateId(String stateId) { this.stateId = stateId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(String manufacturerId) { this.manufacturerId = manufacturerId; }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public List<NetworkPort> getNetworkPorts() { return networkPorts; }
    public void setNetworkPorts(List<NetworkPort> networkPorts) { this.networkPorts = networkPorts; }

    public Infocom getInfocom() { return infocom; }
    public void setInfocom(Infocom infocom) { this.infocom = infocom; }

    public List<String> getContractIds() { return contractIds; }
    public void setContractIds(List<String> contractIds) { this.contractIds = contractIds; }

    public ComputerDetails getComputerDetails() { return computerDetails; }
    public void setComputerDetails(ComputerDetails computerDetails) { this.computerDetails = computerDetails; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
