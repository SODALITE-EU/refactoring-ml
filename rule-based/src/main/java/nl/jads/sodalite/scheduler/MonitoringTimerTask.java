package nl.jads.sodalite.scheduler;

import nl.jads.sodalite.db.MetricsDatabase;
import nl.jads.sodalite.dto.MetricRecord;
import nl.jads.sodalite.utils.PrometheusClient;
import org.json.simple.parser.ParseException;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Logger;

public class MonitoringTimerTask extends TimerTask {
    private static final java.util.logging.Logger log = Logger.getLogger(MonitoringTimerTask.class.getName());
    MetricsDatabase database = MetricsDatabase.getInstance();
    PrometheusClient prometheusClient = new PrometheusClient();

    @Override
    public void run() {
        try {
            List<MetricRecord> metricRecords =
                    prometheusClient.readMetric("http_requests_total");
            for (MetricRecord mr : metricRecords) {
                database.addMetricRecord(mr);
            }
        } catch (ParseException | SQLException e) {
            log.warning(e.getMessage());
        }
        System.out.println("Data Collected at: "
                + LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()),
                ZoneId.systemDefault()));
        try {
            Thread.sleep(60000);
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }
}
