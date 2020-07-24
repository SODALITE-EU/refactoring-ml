import nl.jads.sodalite.db.MetricsDatabase;
import nl.jads.sodalite.dto.DataRecord;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class MetricsDatabaseTest {
    @Test
    public void testMetricDB() {
        MetricsDatabase database = MetricsDatabase.getInstance();
        DataRecord record = new DataRecord();
        record.setLabel("Node1");
        record.setWorkload(122);
        record.setCpu(500.00);
        record.setThermal(56.9);
        record.setMemory(60003.3);
        record.setId(System.currentTimeMillis());
        database.addDataRecord(record);
        DataRecord retrieved = database.getDataRecord("Node1").get(0);
        assertEquals(record.getCpu(), retrieved.getCpu(),0);
    }
}
