package nl.jads.sodalite.utils;

import com.sun.jersey.api.json.JSONConfiguration;
import nl.jads.sodalite.dto.MetricRecord;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class PrometheusClient {
    private static final Logger log = Logger.getLogger(PrometheusClient.class.getName());
    private String baseRestUri;

    public PrometheusClient() {
        baseRestUri = System.getenv("prometheus");
        if (baseRestUri == null || "".equals(baseRestUri.trim())) {
            baseRestUri = "http://154.48.185.213:9090/";
        }
    }

    public List<MetricRecord> readMetric(String query) throws ParseException {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, Boolean.TRUE);
        config.property(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = ClientBuilder.newClient(config);
        WebTarget webTarget = client.target(baseRestUri).path("api/v1/query").queryParam("query", query);
        Invocation.Builder invocationBuilder
                = webTarget.request(MediaType.APPLICATION_JSON);
        String response
                = invocationBuilder.get(String.class);
        JSONParser jsonParser = new JSONParser();

        JSONObject result = (JSONObject) jsonParser.parse(response);
        List<MetricRecord> metricRecords = new ArrayList<>();
        if ("success".equalsIgnoreCase(String.valueOf(result.get("status")))) {
            log.info("Successful collected data: " + query);
            JSONObject dataObject = ((JSONObject) result.get("data"));
            String resultType = String.valueOf(dataObject.get("resultType"));
            JSONArray jsonArray = (JSONArray) dataObject.get("result");
            for (Iterator it = jsonArray.iterator(); it.hasNext(); ) {
                MetricRecord metricRecord = new MetricRecord();
                JSONObject metric = (JSONObject) it.next();
                String mName = String.valueOf(((JSONObject) metric.get("metric")).get("__name__"));
                String label = String.valueOf(((JSONObject) metric.get("metric")).get("instance"));
                JSONArray mValue = (JSONArray) metric.get("value");
                metricRecord.setValueType(resultType);
                metricRecord.setName(mName);
                metricRecord.setValue(mValue);
                metricRecord.setLabel(label);
                metricRecords.add(metricRecord);
            }
        } else {
            log.info("Error collecting data: " + query);
        }
        return metricRecords;
    }
}
