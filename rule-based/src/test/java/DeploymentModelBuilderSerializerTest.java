import nl.jads.sodalite.dto.DeploymentModel;
import nl.jads.sodalite.dto.Node;
import nl.jads.sodalite.utils.DeploymentModelBuilder;
import nl.jads.sodalite.utils.DeploymentModelSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeploymentModelBuilderSerializerTest {


    @Test
    public void testJSON() throws IOException, ParseException {
        DeploymentModel deploymentModel = DeploymentModelBuilder.fromJsonFile("aadm.json");
        assert deploymentModel != null;
        assertEquals(deploymentModel.getVersion(), "1");
        for (Node node : deploymentModel.getNodes()) {
            assertNotNull(node);
            assertNotNull(node.getName());
        }
        JSONObject dmJson = DeploymentModelSerializer.toJson(deploymentModel);
        //Write JSON file
        FileWriter file = new FileWriter("aadm_produced.json");
        file.write(dmJson.toJSONString().replace("\\/","/"));
        file.close();
    }
}
