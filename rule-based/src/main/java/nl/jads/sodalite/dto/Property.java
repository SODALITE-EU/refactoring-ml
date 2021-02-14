package nl.jads.sodalite.dto;

import java.util.ArrayList;
import java.util.List;

public class Property extends NamedElement {
    private String label;
    private List<String> values = new ArrayList<>();

    public void addValue(String value) {
        values.add(value);
    }

    public List<String> getValues() {
        return values;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
