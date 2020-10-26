import nl.jads.sodalite.db.MetricsDatabase;
import nl.jads.sodalite.dto.DataRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        List<DataRecord> retrieved = database.getDataRecord("Node1");
        assertNotNull(retrieved);
    }
}
