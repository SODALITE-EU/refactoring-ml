package nl.jads.sodalite.dto;

import org.json.simple.JSONObject;

public abstract class NamedElement {
    private String name;
    private JSONObject specification;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getSpecification() {
        return specification;
    }

    public void setSpecification(JSONObject specification) {
        this.specification = specification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
