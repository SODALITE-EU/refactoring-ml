package nl.jads.sodalite.dto;

import org.json.simple.JSONObject;

public abstract class NamedElement {
    private String name;
    private JSONObject specification;

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
}
