package nl.jads.sodalite.db;

import nl.jads.sodalite.dto.DataRecord;
import nl.jads.sodalite.dto.MetricRecord;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 To access the metrics database
 */
public class MetricsDatabase {
    private static final Logger log = LogManager.getLogger();
    private static BasicDataSource ds = new BasicDataSource();
    private static MetricsDatabase metricsDatabase = new MetricsDatabase();

    private MetricsDatabase() {
        createDataSource();
        try {
            createTable();
            createRawMetricTable();
        } catch (SQLException ignored) {
        }
    }

    public static MetricsDatabase getInstance() {
        return metricsDatabase;
    }

    private static void createDataSource() {
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:metrics.db");
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }

    private static void createTable() throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS METRICS " +
                    "(ID INT PRIMARY KEY NOT NULL," +
                    " LABEL varchar(255) NOT NULL," +
                    " WORKLOAD           INT, " +
                    " MEMORY            DOUBLE, " +
                    " CPU        DOUBLE, " +
                    " THERMAL         DOUBLE )";
            stmt.executeUpdate(sql);
        }
    }

    private static void createRawMetricTable() throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS RAWMETRICS " +
                    "(ID INT PRIMARY KEY NOT NULL," +
                    " LABEL varchar(255) NOT NULL," +
                    " METRIC varchar(255) NOT NULL," +
                    " VAlUETYPE           varchar(255), " +
                    " VALUE            TEXT)";
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Adding a monitoring data record
     *
     * @param dataRecord a record of monitoring data
     */
    public void addDataRecord(DataRecord dataRecord) throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            String sql =
                    String.format("INSERT INTO METRICS (ID, LABEL, WORKLOAD, " +
                                    "MEMORY, CPU, THERMAL) VALUES ( %d,'%s', %d, %f, %f, %f );",
                            dataRecord.getId(), dataRecord.getLabel(), dataRecord.getWorkload(),
                            dataRecord.getMemory(), dataRecord.getCpu(), dataRecord.getThermal());
            System.out.println(sql);
            stmt.executeUpdate(sql);
        }
    }

    public void addMetricRecord(MetricRecord metricRecord) throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            String sql =
                    String.format("INSERT INTO RAWMETRICS (ID, LABEL, METRIC, VALUETYPE, " +
                                    "VALUE) VALUES ( %d,'%s','%s', '%s', '%s');",
                            System.currentTimeMillis(), metricRecord.getLabel(), metricRecord.getName(),
                            metricRecord.getValueType(), metricRecord.getValue().toJSONString());
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Get the records of monitoring data per node
     *
     * @param label the label of a node
     * @return a record of monitoring data
     */
    public List<DataRecord> getDataRecord(String label) throws SQLException {
        List<DataRecord> dataRecords = new ArrayList<>();
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            ResultSet rs =
                    stmt.executeQuery(String.format("SELECT * FROM METRICS WHERE LABEL='%s';", label));

            while (rs.next()) {
                DataRecord dataRecord = new DataRecord();
                dataRecord.setLabel(label);
                dataRecord.setId(rs.getLong("id"));
                dataRecord.setWorkload(rs.getInt("workload"));
                dataRecord.setCpu(rs.getDouble("cpu"));
                dataRecord.setMemory(rs.getDouble("memory"));
                dataRecord.setThermal(rs.getDouble("thermal"));
                dataRecords.add(dataRecord);
            }
            rs.close();
        }
        return dataRecords;
    }

    /**
     * Get the records of monitoring data per node
     *
     * @param label the label of a node
     * @return a record of monitoring data
     */
    public List<MetricRecord> getMetricRecord(String label) throws SQLException {
        List<MetricRecord> dataRecords = new ArrayList<>();
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            ResultSet rs =
                    stmt.executeQuery(String.format("SELECT * FROM RAWMETRICS WHERE LABEL='%s';", label));

            while (rs.next()) {
                MetricRecord dataRecord = new MetricRecord();
                dataRecord.setLabel(label);
                dataRecord.setLabel(rs.getString("label"));
                dataRecord.setName(rs.getString("metric"));
                dataRecord.setValueType(rs.getString("valuetype"));
                JSONParser parser = new JSONParser();
                try {
                    dataRecord.setValue((JSONArray) parser.parse(rs.getString("value")));
                } catch (ParseException e) {
                    log.error(e.getMessage());
                }
                dataRecords.add(dataRecord);
            }
            rs.close();
        }
        return dataRecords;
    }
}
