package nl.jads.sodalite.rules;

public class AttributedSelectedEvent implements IEvent{
    private String name;
    private String value;
    private String vsnId;
    private String intValue;

    public AttributedSelectedEvent(String name, String value, String vsnId) {
        this.name = name;
        this.value = value;
        this.vsnId = vsnId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getVsnId() {
        return vsnId;
    }

    public void setVsnId(String vsnId) {
        this.vsnId = vsnId;
    }

    public String getIntValue() {
        return intValue;
    }

    public void setIntValue(String intValue) {
        this.intValue = intValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
