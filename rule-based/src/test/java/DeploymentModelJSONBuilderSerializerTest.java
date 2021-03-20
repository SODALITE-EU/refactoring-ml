import nl.jads.sodalite.dto.AADMModel;
import nl.jads.sodalite.dto.DeploymentModelJSON;
import nl.jads.sodalite.dto.Node;
import nl.jads.sodalite.utils.AADMModelBuilder;
import nl.jads.sodalite.utils.DeploymentModelBuilder;
import nl.jads.sodalite.utils.DeploymentModelSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeploymentModelJSONBuilderSerializerTest {


    @Test
    public void testJSON() throws IOException, ParseException {
        DeploymentModelJSON deploymentModelJSON = DeploymentModelBuilder.fromJsonFile("aadm.json");
        assert deploymentModelJSON != null;
        assertEquals(deploymentModelJSON.getVersion(), "1");
        for (Node node : deploymentModelJSON.getNodes()) {
            assertNotNull(node);
            assertNotNull(node.getName());
        }
        JSONObject dmJson = DeploymentModelSerializer.toJson(deploymentModelJSON);
        assertNotNull(dmJson);
    }

    @Test
    public void testJSONAADM() throws IOException, ParseException {
        AADMModel deploymentModelJSON = AADMModelBuilder.fromJsonFile("aadm_simple.json");
        assert deploymentModelJSON != null;
        assertEquals(23, deploymentModelJSON.getNodes().size());
        assertEquals(15, deploymentModelJSON.getInputs().size());
        assertNotNull(deploymentModelJSON.getExchangeAADM());
    }
}
