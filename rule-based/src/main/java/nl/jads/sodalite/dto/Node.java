package nl.jads.sodalite.dto;

import kb.dto.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Node {
    private String name;
    private String type;
    private Map<String, Property> properties = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addProperty(Property node) {
        properties.putIfAbsent(node.getName(), node);
    }

    public Property getProperty(String pName) {
        return properties.get(pName);


    }

    public Collection<Property> getProperties() {
        return properties.values();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
