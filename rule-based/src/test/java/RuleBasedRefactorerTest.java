import nl.jads.sodalite.dto.BuleprintsData;
import nl.jads.sodalite.utils.POJOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class RuleBasedRefactorerTest {

    @Test
    public void testJSON() {
        BuleprintsData[] buleprintsDatas = POJOFactory.fromJsonFile("blueprintdata.json");
        assertEquals(buleprintsDatas.length, 2);
        assertEquals(buleprintsDatas[0].getTarget().length, 2);
        assertEquals(buleprintsDatas[1].getBlueprint().getVersionId(), 1);
        assertEquals(buleprintsDatas[1].getBlueprint().getBlueprintToken(), "97241299-8de9-430b-a9b5-adf0f4437d23");
        assertEquals(buleprintsDatas[1].getInput(), "vehicle-iot-v2/input.xml");
    }
}
