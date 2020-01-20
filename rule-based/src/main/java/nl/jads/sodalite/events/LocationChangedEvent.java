package nl.jads.sodalite.events;

public class LocationChangedEvent implements IEvent {
    private String preLoc;
    private String currentLoc;

    public LocationChangedEvent(String preLoc, String currentLoc) {
        this.preLoc = preLoc;
        this.currentLoc = currentLoc;
    }

    public String getPreLoc() {
        return preLoc;
    }

    public void setPreLoc(String preLoc) {
        this.preLoc = preLoc;
    }

    public String getCurrentLoc() {
        return currentLoc;
    }

    public void setCurrentLoc(String currentLoc) {
        this.currentLoc = currentLoc;
    }
}
