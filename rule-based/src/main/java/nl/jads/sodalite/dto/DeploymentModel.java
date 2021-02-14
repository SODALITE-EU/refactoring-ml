package nl.jads.sodalite.dto;

import nl.jads.sodalite.events.IEvent;
import org.json.simple.JSONArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DeploymentModel implements IEvent {
    private String id;
    private String namespace;
    private String createdBy;
    private String createdAt;
    private String version;
    private JSONArray participants;
    private Map<String, Node> nodes = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JSONArray getParticipants() {
        return participants;
    }

    public void setParticipants(JSONArray participants) {
        this.participants = participants;
    }

    public void addNode(Node node) {
        nodes.putIfAbsent(node.getName(), node);
    }

    public Node getNode(String nodeName) {
        return nodes.get(nodeName);


    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }
}
