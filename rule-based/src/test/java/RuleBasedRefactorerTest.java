import nl.jads.sodalite.dto.BuleprintsData;
import nl.jads.sodalite.dto.POJOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class RuleBasedRefactorerTest {

    @Test
    public void testJSON() {
        BuleprintsData[] buleprintsDatas = POJOFactory.fromJsonFile("blueprintdata.json", getClass());
        assertEquals(buleprintsDatas.length, 2);
        assertEquals(buleprintsDatas[0].getTarget().length, 2);
        assertEquals(buleprintsDatas[1].getBlueprint().getVersionId(), 1);
        assertEquals(buleprintsDatas[1].getBlueprint().getBlueprintToken(), "string");
    }
}
