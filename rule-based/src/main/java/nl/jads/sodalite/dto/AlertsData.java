package nl.jads.sodalite.dto;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class AlertsData {
    @JsonProperty("version")
    private String version;
    @JsonProperty("groupKey")
    private String groupKey;
    @JsonProperty("truncatedAlerts")
    private int truncatedAlerts;
    @JsonProperty("status")
    private String status;
    @JsonProperty("receiver")
    private String receiver;
    @JsonProperty("groupLabels")
    private Object groupLabels;
    @JsonProperty("commonLabels")
    private Object commonLabels;
    @JsonProperty("commonAnnotations")
    private Object commonAnnotations;
    @JsonProperty("externalURL")
    private String externalURL;
    @JsonProperty("alerts")
    private AlertDTO[] alertDTOS;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public int getTruncatedAlerts() {
        return truncatedAlerts;
    }

    public void setTruncatedAlerts(int truncatedAlerts) {
        this.truncatedAlerts = truncatedAlerts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @JsonAnyGetter
    public Object getGroupLabels() {
        return groupLabels;
    }

    @JsonAnySetter
    public void setGroupLabels(Object groupLabels) {
        this.groupLabels = groupLabels;
    }

    @JsonAnyGetter
    public Object getCommonLabels() {
        return commonLabels;
    }

    @JsonAnySetter
    public void setCommonLabels(Object commonLabels) {
        this.commonLabels = commonLabels;
    }

    @JsonAnyGetter
    public Object getCommonAnnotations() {
        return commonAnnotations;
    }

    @JsonAnySetter
    public void setCommonAnnotations(Object commonAnnotations) {
        this.commonAnnotations = commonAnnotations;
    }

    public String getExternalURL() {
        return externalURL;
    }

    public void setExternalURL(String externalURL) {
        this.externalURL = externalURL;
    }

    public AlertDTO[] getAlertDTOS() {
        return alertDTOS;
    }

    public void setAlertDTOS(AlertDTO[] alertDTOS) {
        this.alertDTOS = alertDTOS;
    }

    @Override
    public String toString() {
        return "AlertsData{" +
                "version='" + version + '\'' +
                ", groupKey='" + groupKey + '\'' +
                ", truncatedAlerts=" + truncatedAlerts +
                ", status='" + status + '\'' +
                ", receiver='" + receiver + '\'' +
                ", groupLabels=" + groupLabels +
                ", commonLabels=" + commonLabels +
                ", commonAnnotations=" + commonAnnotations +
                ", externalURL='" + externalURL + '\'' +
                ", alerts=" + Arrays.toString(alertDTOS) +
                '}';
    }
}
