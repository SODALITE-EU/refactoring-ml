import nl.jads.sodalite.dto.DeploymentModel;
import nl.jads.sodalite.dto.Node;
import nl.jads.sodalite.utils.DeploymentModelBuilder;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeploymentModelBuilderTest {


    @Test
    public void testJSON() throws IOException, ParseException {
        DeploymentModel deploymentModel = DeploymentModelBuilder.fromJsonFile("aadm.json");
        assert deploymentModel != null;
        assertEquals(deploymentModel.getVersion(), "1");
        for (Node node: deploymentModel.getNodes()){
            System.out.println(node.getName());
            assertNotNull(node);
        }
    }
}
