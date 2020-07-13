package nl.jads.sodalite.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Alert {
    @JsonProperty("status")
    private String status;
    @JsonProperty("labels")
    private Object labels;
    @JsonProperty("annotations")
    private Object annotations;
    @JsonProperty("startsAt")
    private String startsAt;
    @JsonProperty("endsAt")
    private String endsAt;
    @JsonProperty("generatorURL")
    private String generatorURL;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonAnyGetter
    public Object getLabels() {
        return labels;
    }

    @JsonAnySetter
    public void setLabels(Object labels) {
        this.labels = labels;
    }

    @JsonAnyGetter
    public Object getAnnotations() {
        return annotations;
    }

    @JsonAnySetter
    public void setAnnotations(Object annotations) {
        this.annotations = annotations;
    }

    public String getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(String startsAt) {
        this.startsAt = startsAt;
    }

    public String getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(String endsAt) {
        this.endsAt = endsAt;
    }

    public String getGeneratorURL() {
        return generatorURL;
    }

    public void setGeneratorURL(String generatorURL) {
        this.generatorURL = generatorURL;
    }
}
