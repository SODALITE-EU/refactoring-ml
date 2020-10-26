import nl.jads.sodalite.dto.BuleprintsDataSet;
import nl.jads.sodalite.utils.POJOFactory;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class RuleBasedRefactorerTest {

    @Test
    public void testJSON() {
        BuleprintsDataSet buleprintsDatas = POJOFactory.fromJsonFile("blueprintdata.json");
        assert buleprintsDatas != null;
        assertEquals(buleprintsDatas.getBlueprints().length, 2);
        assertEquals(buleprintsDatas.getBlueprints()[0].getTarget().length, 2);
    }
}
