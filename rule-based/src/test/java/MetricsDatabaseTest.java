import nl.jads.sodalite.db.MetricsDatabase;
import nl.jads.sodalite.dto.DataRecord;
import nl.jads.sodalite.dto.MetricRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
        for (DataRecord dr : retrieved) {
            assertNotNull(String.valueOf(dr.getId()));
            assertEquals(122, dr.getWorkload());
            assertEquals(500.00, dr.getCpu(), 0.0);
            assertEquals(56.9, dr.getThermal(), 0.0);
            assertEquals(60003.3, dr.getMemory(), 0.0);
        }
        MetricRecord metricRecord = new MetricRecord();
        metricRecord.setLabel("Node2");
        metricRecord.setValueType("CPUUsage");
        metricRecord.setName("CPUVM");
        JSONObject jo = new JSONObject();
        jo.put("value", 85);
        JSONArray ja = new JSONArray();
        ja.add(jo);
        metricRecord.setValue(ja);
        database.addMetricRecord(metricRecord);
        List<MetricRecord> mrList = database.getMetricRecord("Node2");
        assertNotNull(retrieved);
        for (MetricRecord mr : mrList) {
            assertNotNull(mr.getValue());
            assertEquals("CPUVM", mr.getName());
            assertEquals("CPUUsage", mr.getValueType());
        }
    }
}
