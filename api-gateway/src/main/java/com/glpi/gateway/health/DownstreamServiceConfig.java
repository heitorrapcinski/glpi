package com.glpi.gateway.health;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for downstream service URLs used in health aggregation.
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class DownstreamServiceConfig {

    private List<ServiceEntry> downstreamServices = new ArrayList<>();

    public List<ServiceEntry> getDownstreamServices() {
        return downstreamServices;
    }

    public void setDownstreamServices(List<ServiceEntry> downstreamServices) {
        this.downstreamServices = downstreamServices;
    }

    public static class ServiceEntry {
        private String name;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
