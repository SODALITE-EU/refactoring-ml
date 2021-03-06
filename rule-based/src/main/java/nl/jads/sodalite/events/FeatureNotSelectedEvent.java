package nl.jads.sodalite.events;

public class FeatureNotSelectedEvent implements IEvent {
    private String name;
    private String vsnId;

    public FeatureNotSelectedEvent(String name, String vsnId) {
        this.name = name;
        this.vsnId = vsnId;
    }

    public String getVsnId() {
        return vsnId;
    }

    public void setVsnId(String vsnId) {
        this.vsnId = vsnId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
