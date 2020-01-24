package nl.jads.sodalite.events;

public class DeploymentRemove implements IEvent {
    private String preLoc;

    public DeploymentRemove(String preLoc) {
        this.preLoc = preLoc;
    }

    public String getPreLoc() {
        return preLoc;
    }

    public void setPreLoc(String preLoc) {
        this.preLoc = preLoc;
    }
}
