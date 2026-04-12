package com.glpi.asset.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Computer-specific embedded sub-document.
 * Requirements: 12.9
 */
public class ComputerDetails {

    private Map<String, String> osInstallation;
    private List<Map<String, Object>> devices;
    private List<Map<String, Object>> diskPartitions;
    private List<Map<String, Object>> softwareInstallations;
    private List<Map<String, Object>> virtualMachines;

    public ComputerDetails() {
        this.devices = new ArrayList<>();
        this.diskPartitions = new ArrayList<>();
        this.softwareInstallations = new ArrayList<>();
        this.virtualMachines = new ArrayList<>();
    }

    public Map<String, String> getOsInstallation() { return osInstallation; }
    public void setOsInstallation(Map<String, String> osInstallation) { this.osInstallation = osInstallation; }

    public List<Map<String, Object>> getDevices() { return devices; }
    public void setDevices(List<Map<String, Object>> devices) { this.devices = devices; }

    public List<Map<String, Object>> getDiskPartitions() { return diskPartitions; }
    public void setDiskPartitions(List<Map<String, Object>> diskPartitions) { this.diskPartitions = diskPartitions; }

    public List<Map<String, Object>> getSoftwareInstallations() { return softwareInstallations; }
    public void setSoftwareInstallations(List<Map<String, Object>> softwareInstallations) { this.softwareInstallations = softwareInstallations; }

    public List<Map<String, Object>> getVirtualMachines() { return virtualMachines; }
    public void setVirtualMachines(List<Map<String, Object>> virtualMachines) { this.virtualMachines = virtualMachines; }
}
