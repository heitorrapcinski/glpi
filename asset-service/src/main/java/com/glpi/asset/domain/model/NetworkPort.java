package com.glpi.asset.domain.model;

/**
 * Network port value object attached to an asset.
 * Requirements: 12.10
 */
public class NetworkPort {

    private String id;
    private String name;
    private String mac;
    private String ip;
    private String vlan;
    private String connectionType;

    public NetworkPort() {}

    public NetworkPort(String id, String name, String mac, String ip, String vlan, String connectionType) {
        this.id = id;
        this.name = name;
        this.mac = mac;
        this.ip = ip;
        this.vlan = vlan;
        this.connectionType = connectionType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getVlan() { return vlan; }
    public void setVlan(String vlan) { this.vlan = vlan; }

    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
}
