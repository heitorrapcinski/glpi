package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for the assets collection (polymorphic via assetType).
 * Requirements: 22.6
 */
@Document(collection = "assets")
public class AssetDocument {

    @Id
    private String id;

    @Indexed
    private AssetType assetType;

    private String name;

    @Indexed
    private String entityId;

    private String serial;
    private String otherSerial;
    private String stateId;
    private String locationId;

    @Indexed
    private String userId;

    @Indexed
    private String groupId;

    private String manufacturerId;
    private String modelId;

    @Indexed
    private boolean isDeleted;

    private List<NetworkPort> networkPorts = new ArrayList<>();
    private Infocom infocom;
    private List<String> contractIds = new ArrayList<>();
    private ComputerDetails computerDetails;
    private Instant createdAt;
    private Instant updatedAt;

    public AssetDocument() {}

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
