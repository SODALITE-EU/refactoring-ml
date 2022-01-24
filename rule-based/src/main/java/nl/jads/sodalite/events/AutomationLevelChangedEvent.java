package nl.jads.sodalite.events;

public class AutomationLevelChangedEvent implements IEvent {
    private int preLevel;
    private int newLevel;

    public AutomationLevelChangedEvent(int preLevel, int newLevel) {
        this.preLevel = preLevel;
        this.newLevel = newLevel;
    }

    public int getPreLevel() {
        return preLevel;
    }

    public void setPreLevel(int preLevel) {
        this.preLevel = preLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }
}
