package nl.jads.sodalite.dto;

import com.google.gson.JsonObject;

public class IaCBuilderInput {
    private String name;
    private JsonObject data;

    public void setName(String name) {
        this.name = name;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
