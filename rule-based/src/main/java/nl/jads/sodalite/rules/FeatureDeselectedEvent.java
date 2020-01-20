package nl.jads.sodalite.rules;

public class FeatureDeselectedEvent implements IEvent {
    private String name;
    private String vsnId;

    public FeatureDeselectedEvent(String name, String vsnId) {
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
}
