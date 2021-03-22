package nl.jads.sodalite.dto;

import tosca.mapper.dto.Node;
import tosca.mapper.dto.Property;
import tosca.mapper.exchange.generator.AADMGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AADMModel {
    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Property> inputs = new HashMap<>();
    private String id;
    private String version;
    private String namespace;

    public void addNode(Node node) {
        nodes.putIfAbsent(node.getName(), node);
    }

    public Node getNode(String name) {
        return nodes.get(name);
    }

    public void addInput(Property input) {
        inputs.putIfAbsent(input.getName(), input);
    }

    public Property getInput(String name) {
        return inputs.get(name);
    }

    public void addProperties(Set<Property> properties) {
        properties.forEach(this::addInput);
    }

    public Set<Property> getInputs() {
        return new HashSet<Property>(inputs.values());
    }

    public Set<Node> getNodes() {
        return new HashSet<Node>(nodes.values());
    }

    public String getExchangeAADM() {
        AADMGenerator aadm = new AADMGenerator(getNodes(), getInputs());
        aadm.convertModelToExchange();
        return aadm.getExchangeModel();
    }

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void updateNodeTypes() {
        for (Node node : getNodes()) {
//            node.setName(namespace +"/"+ node.getName())
            for (tosca.mapper.dto.Requirement requirement : node.getRequirements()) {
                requirement.setValue(namespace + "/" + requirement.getValue());
            }
            for (tosca.mapper.dto.Capability capability : node.getCapabilities()) {
                capability.setValue(namespace + "/" + capability.getValue());
            }
        }
    }
}
