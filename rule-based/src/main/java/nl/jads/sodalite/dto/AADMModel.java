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
}
