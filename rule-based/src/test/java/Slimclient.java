import nl.jads.sodalite.db.MetricsDatabase;
import nl.jads.sodalite.dto.MetricRecord;
import nl.jads.sodalite.utils.PrometheusClient;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class Slimclient {
    private static final String TARGET_URL = "http://154.48.185.206:5000/deploy/a6442450-deb6-4b61-8c95-446bd095b8f0";

    public Slimclient() {
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(TARGET_URL);
        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("timestamp", "2020-01-24T10:43:20.993831")
                        .field("version_id", String.valueOf(1));
        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("inputs_file",
                new File("C:\\Postdoc\\projects\\sodalite-eu\\refactoring-ml\\rule-based\\src\\test\\resources\\vehicle-iot-v1\\input.yaml"),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .build("DELETE", Entity.entity(multipart, multipart.getMediaType()))
                .invoke();
        response.readEntity(String.class);
        response.close();
    }

    public static void main(String[] args) throws ParseException, SQLException {
        PrometheusClient prometheusClient = new PrometheusClient();
        List<MetricRecord> metricRecords =
                prometheusClient.readMetric("http_requests_total");

        for (MetricRecord mr : metricRecords) {
            JSONArray jsonArray = mr.getValue();
            Vector2D vector2D = new Vector2D(Double.parseDouble(String.valueOf(jsonArray.get(0))),
                    Double.parseDouble(String.valueOf(jsonArray.get(1))));
            MetricsDatabase database = MetricsDatabase.getInstance();
            database.addMetricRecord(mr);
            MetricRecord record =
                    database.getMetricRecord(mr.getLabel()).get(0);
        }
    }
}
