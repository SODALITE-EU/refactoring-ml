package nl.jads.sodalite.dto;

public class BuleprintsData {
    private String[] target;
    private BlueprintMetadata blueprint;

    public String[] getTarget() {
        return target;
    }

    public void setTarget(String[] target) {
        this.target = target;
    }

    public BlueprintMetadata getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(BlueprintMetadata blueprint) {
        this.blueprint = blueprint;
    }
}
