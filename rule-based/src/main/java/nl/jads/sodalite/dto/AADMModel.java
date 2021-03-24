package nl.jads.sodalite.dto;

import tosca.mapper.dto.Node;
import tosca.mapper.dto.Parameter;
import tosca.mapper.dto.Property;
import tosca.mapper.dto.Requirement;
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

    public void removeNode(String name) {
        nodes.remove(name);
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
                for (Parameter parameter : requirement.getParameters()) {
                    String pValue = parameter.getValue();
                    if (!(pValue.contains(DTOConstraints.KUBE)
                            | pValue.contains(DTOConstraints.DOCKER)
                            | pValue.contains(DTOConstraints.OPENSTACK))) {
                        parameter.setValue(namespace + "/" + parameter.getValue());
                    }
                }
            }
            for (tosca.mapper.dto.Capability capability : node.getCapabilities()) {
                if (!(capability.getValue().contains(DTOConstraints.KUBE)
                        | capability.getValue().contains(DTOConstraints.DOCKER)
                        | capability.getValue().contains(DTOConstraints.OPENSTACK))) {
                    capability.setValue(namespace + "/" + capability.getValue());
                }
            }
        }
    }


    public void updateProperty(String nodeName, String name, String value) {
        for (Property p : getNode(nodeName).getProperties()) {
            if (p.getName().equals(name)) {
                p.setValue(value);
            }
        }
    }

    public void updateRequirement(String nodeName, String name, String value) {
        for (Requirement requirement : getNode(nodeName).getRequirements()) {
            if (requirement.getName().equals(name)) {
                for (Parameter parameter : requirement.getParameters()) {
                    if (value.contains(DTOConstraints.KUBE)
                            | value.contains(DTOConstraints.DOCKER)
                            | value.contains(DTOConstraints.OPENSTACK)) {
                        parameter.setValue(value);
                    } else {
                        parameter.setValue(namespace + "/" + value);
                    }
                }
            }
        }
    }
}
