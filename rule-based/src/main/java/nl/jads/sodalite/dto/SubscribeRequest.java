package nl.jads.sodalite.dto;

import com.google.gson.annotations.Expose;
import nl.jads.sodalite.events.ResourceEvent;

public class SubscribeRequest {
    @Expose
    private String namespace;
    @Expose
    private String endpoint;
    @Expose
    private ResourceEvent[] payload;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public ResourceEvent[] getPayload() {
        return payload;
    }

    public void setPayload(ResourceEvent[] payload) {
        this.payload = payload;
    }
}
