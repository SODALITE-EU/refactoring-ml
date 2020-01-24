package nl.jads.sodalite.dto;

public class BuleprintsData {
    private String[] target;
    private BlueprintMetadata blueprint;
    private String input;

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

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
