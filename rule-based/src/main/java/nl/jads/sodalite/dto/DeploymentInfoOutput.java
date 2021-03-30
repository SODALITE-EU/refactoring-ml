package nl.jads.sodalite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeploymentInfoOutput {
    @JsonProperty("original")
    private DeploymentInfo original;
    @JsonProperty("current")
    private DeploymentInfo current;
    @JsonProperty("appid")
    private String appid;

    public DeploymentInfo getOriginal() {
        return original;
    }

    public void setOriginal(DeploymentInfo original) {
        this.original = original;
    }

    public DeploymentInfo getCurrent() {
        return current;
    }

    public void setCurrent(DeploymentInfo current) {
        this.current = current;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }
}
