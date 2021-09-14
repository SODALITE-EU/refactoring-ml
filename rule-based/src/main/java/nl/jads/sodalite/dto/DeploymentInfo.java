package nl.jads.sodalite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.Map;

public class DeploymentInfo {
    @JsonProperty("aadm_id")
    private String aadm_id;
    @JsonProperty("deployment_id")
    private String deployment_id;
    @JsonProperty("blueprint_id")
    private String blueprint_id;
    @JsonProperty("inputs")
    private String inputs;
    @JsonProperty("version")
    private String version;
    private Map<String, Object> inputMap;

    public String getAadm_id() {
        return aadm_id;
    }

    public void setAadm_id(String aadm_id) {
        this.aadm_id = aadm_id;
    }

    public String getDeployment_id() {
        return deployment_id;
    }

    public void setDeployment_id(String deployment_id) {
        this.deployment_id = deployment_id;
    }

    public String getBlueprint_id() {
        return blueprint_id;
    }

    public void setBlueprint_id(String blueprint_id) {
        this.blueprint_id = blueprint_id;
    }

    public String getInputs() {
        return inputs;
    }

    public void setInput(String inputs) {
        this.inputs = inputs;
        toYaml();
    }

    private void toYaml() {
        Yaml yaml = new Yaml();
        inputMap = yaml.load(inputs);
    }

    public void updateInput(String key, String value) {
        inputMap.put(key, value);
    }

    public String getUpdatedInput() {
        Yaml yaml = new Yaml();
        StringWriter writer = new StringWriter();
        yaml.dump(inputMap, writer);
        return writer.toString();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
