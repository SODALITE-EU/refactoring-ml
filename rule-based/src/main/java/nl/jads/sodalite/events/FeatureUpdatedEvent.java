package nl.jads.sodalite.events;

public class FeatureUpdatedEvent implements IEvent {
    private String name;
    private String vsnId;

    public FeatureUpdatedEvent(String name, String vsnId) {
        this.name = name;
        this.vsnId = vsnId;
    }

    public String getVsnId() {
        return vsnId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVsnId(String vsnId) {
        this.vsnId = vsnId;
    }
}
