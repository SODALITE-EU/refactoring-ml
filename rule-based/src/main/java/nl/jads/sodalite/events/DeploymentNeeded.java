package nl.jads.sodalite.events;

public class DeploymentNeeded implements IEvent {
    private String currentLoc;

    public DeploymentNeeded(String currentLoc) {
        this.currentLoc = currentLoc;
    }

    public String getCurrentLoc() {
        return currentLoc;
    }

    public void setCurrentLoc(String currentLoc) {
        this.currentLoc = currentLoc;
    }
}
