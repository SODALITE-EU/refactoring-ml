package nl.jads.sodalite.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceEvent implements  IEvent{
    @JsonProperty("e_type")
    String eType;
    @JsonProperty("r_type")
    String rType;

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
}
