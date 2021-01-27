import nl.jads.sodalite.db.MetricsDatabase;
import nl.jads.sodalite.dto.DataRecord;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetricsDatabaseTest {
    @Test
    public void testMetricDB() throws SQLException {
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
        for (DataRecord mr : retrieved) {
            assertNotNull(String.valueOf(mr.getId()));
            assertEquals(122, mr.getWorkload());
            assertEquals(500.00, mr.getCpu(), 0.0);
            assertEquals(56.9, mr.getThermal(), 0.0);
            assertEquals(60003.3, mr.getMemory(), 0.0);
        }
    }
}
