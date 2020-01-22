package nl.jads.sodalite.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InputEventData {
    private String eventType;
    private String previousLocation;
    private String newLocation;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPreviousLocation() {
        return previousLocation;
    }

    public void setPreviousLocation(String previousLocation) {
        this.previousLocation = previousLocation;
    }

    public String getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(String newLocation) {
        this.newLocation = newLocation;
    }

    @Override
    public String toString() {
        return "InputEventData{" +
                "eventType='" + eventType + '\'' +
                ", previousLocation='" + previousLocation + '\'' +
                ", newLocation='" + newLocation + '\'' +
                '}';
    }
}
