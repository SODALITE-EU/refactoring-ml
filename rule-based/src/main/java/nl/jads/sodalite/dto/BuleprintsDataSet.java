package nl.jads.sodalite.dto;

public class BuleprintsDataSet {
    private String input;
    private BuleprintsData[] blueprints;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public BuleprintsData[] getBlueprints() {
        return blueprints;
    }

    public void setBlueprints(BuleprintsData[] blueprints) {
        this.blueprints = blueprints;
    }
}
