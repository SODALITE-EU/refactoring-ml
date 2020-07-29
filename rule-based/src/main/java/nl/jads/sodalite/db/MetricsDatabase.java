package nl.jads.sodalite.db;

import nl.jads.sodalite.dto.DataRecord;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        createTable();
    }

    private static Connection getConnection() throws SQLException {
        return ds.getConnection();
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

    private static void createTable() {
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = ds.getConnection();
            stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS METRICS " +
                    "(ID INT PRIMARY KEY NOT NULL," +
                    " LABEL varchar(255) NOT NULL," +
                    " WORKLOAD           INT, " +
                    " MEMORY            DOUBLE, " +
                    " CPU        DOUBLE, " +
                    " THERMAL         DOUBLE )";
            stmt.executeUpdate(sql);
            if (log.isInfoEnabled()) {
                log.info("Metrics table was created successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Adding a monitoring data record
     *
     * @param dataRecord a record of monitoring data
     */
    public void addDataRecord(DataRecord dataRecord) {
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = ds.getConnection();
            stmt = connection.createStatement();
            String sql =
                    String.format("INSERT INTO METRICS (ID, LABEL, WORKLOAD, " +
                                    "MEMORY, CPU, THERMAL) VALUES ( %d,'%s', %d, %f, %f, %f );",
                            dataRecord.getId(), dataRecord.getLabel(), dataRecord.getWorkload(),
                            dataRecord.getMemory(), dataRecord.getCpu(), dataRecord.getThermal());
            System.out.println(sql);
            stmt.executeUpdate(sql);
            if (log.isInfoEnabled()) {
                log.info("Metrics table was created successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Get the records of monitoring data per node
     *
     * @param label the label of a node
     * @return a record of monitoring data
     */
    public List<DataRecord> getDataRecord(String label) {
        List<DataRecord> dataRecords = new ArrayList<>();
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = ds.getConnection();
            stmt = connection.createStatement();
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
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
        return dataRecords;
    }
}