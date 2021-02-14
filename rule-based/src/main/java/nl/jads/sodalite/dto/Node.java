package nl.jads.sodalite.dto;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Node {
    private String name;
    private String type;
    private Map<String, Requirement> requirements = new HashMap<>();
    private Map<String, Property> properties = new HashMap<>();
    private Map<String, Attribute> attributes = new HashMap<>();
    private Map<String, Capability> capabilities = new HashMap<>();
    private Map<String, Interface> interfaces = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addRequirement(Requirement requirement) {
        requirements.putIfAbsent(requirement.getName(), requirement);
    }

    public Requirement getRequirement(String pName) {
        return requirements.get(pName);
    }

    public Collection<Requirement> getRequirements() {
        return requirements.values();
    }

    public void addProperty(Property property) {
        properties.putIfAbsent(property.getName(), property);
    }

    public Property getProperty(String pName) {
        return properties.get(pName);
    }

    public Collection<Property> getProperties() {
        return properties.values();
    }

    public void addAttribute(Attribute property) {
        attributes.putIfAbsent(property.getName(), property);
    }

    public Attribute getAttribute(String pName) {
        return attributes.get(pName);
    }

    public Collection<Attribute> getAttributes() {
        return attributes.values();
    }

    public void addCapability(Capability capability) {
        capabilities.putIfAbsent(capability.getName(), capability);
    }

    public Capability getCapability(String pName) {
        return capabilities.get(pName);
    }

    public Collection<Capability> getCapabilities() {
        return capabilities.values();
    }

    public void addInterface(Interface anInterface) {
        interfaces.putIfAbsent(anInterface.getName(), anInterface);
    }

    public Interface getInterface(String pName) {
        return interfaces.get(pName);
    }

    public Collection<Interface> getInterfaces() {
        return interfaces.values();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
