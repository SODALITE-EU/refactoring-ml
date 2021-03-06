package nl.jads.sodalite.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import tosca.mapper.dto.Node;

public class ResourceEvent implements  IEvent{
    @JsonProperty("e_type")
    @Expose
    @SerializedName(value = "e_type")
    String eType;
    @JsonProperty("r_type")
    @Expose
    @SerializedName(value = "r_type")
    String rType;
    private Node node;

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
