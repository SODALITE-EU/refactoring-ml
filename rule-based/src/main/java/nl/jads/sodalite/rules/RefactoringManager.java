package nl.jads.sodalite.rules;

import nl.jads.sodalite.dto.BuleprintsData;
import nl.jads.sodalite.dto.BuleprintsDataSet;
import nl.jads.sodalite.utils.POJOFactory;
import nl.jads.sodalite.utils.ResourceUtil;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RefactoringManager {
    private static final String BASE_REST_URI
            = "http://154.48.185.209:5000/";
    private Map<String, BuleprintsData> mapBM = new HashMap<String, BuleprintsData>();
    private String currentBlueprintToken;
    private String getBaseRestUri;
    private String input;

    public RefactoringManager() {
        getBaseRestUri = System.getenv("xoperarest");
        if (getBaseRestUri == null || "".equals(getBaseRestUri.trim())) {
            getBaseRestUri = BASE_REST_URI;
        }
        BuleprintsDataSet buleprintsDatas = POJOFactory.fromJsonFile("blueprintdata.json");
        if (buleprintsDatas != null) {
            configure(buleprintsDatas);
        }
    }

    public void addDeploymentOption(String name, String vsnId, Map<String, String> parameters) {
//        createVSN(vsnId);
//        opMgt.addRegulationUnitsToProcessRegulationPolicy(vsnId, vsnId, name);
    }

    public void addDeploymentOption(String name, String vsnId) {
//        createVSN(vsnId);
//        opMgt.addRegulationUnitsToProcessRegulationPolicy(vsnId, vsnId, name);
    }

    public void removeDeploymentOption(String name, String vsnId) {
//        opMgt.removeRegulationUnitsFromProcessRegulationPolicy(vsnId, vsnId, name);
    }

    public void updateDeploymentOption(String name, String vsnId, Map<String, String> parameters) {
        //  opMgt.updateRegulationUnitsOfProcessRegulationPolicy()
    }

    public void updateDeploymentOption(String name, String vsnId) {
        //  opMgt.updateRegulationUnitsOfProcessRegulationPolicy()
    }

    public String getCurrentBlueprintToken() {
        return currentBlueprintToken;
    }

    public void setCurrentBlueprintToken(String currentBlueprintToken) {
        this.currentBlueprintToken = currentBlueprintToken;
    }

    public Collection<BuleprintsData> getAllBlueprintMetadata() {
        return mapBM.values();
    }

    public Set<String> getAllTokens() {
        return mapBM.keySet();
    }

    public void deployDeploymentModel(String target) {
        BuleprintsData blueprintsData = mapBM.get(target);
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(getBaseRestUri).path("deploy/" + blueprintsData.getBptoken());

        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("blueprint_token", blueprintsData.getBptoken());

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getStringAsFile(input),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        String message = response.readEntity(String.class);
        response.close();

        System.out.println(message);
        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
        System.out.println(" Deployment Status for " + target + "," + blueprintsData.getBptoken());
        System.out.println(response.getStatus());
        System.out.println(message);
        System.out.println();
        currentBlueprintToken = blueprintsData.getBptoken();
    }

    public void undeployDeploymentModel(String target) {
        BuleprintsData blueprintsData = mapBM.get(target);
        ClientConfig config = new ClientConfig((MultiPartFeature.class));
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        Client client = ClientBuilder.newClient(config);
        WebTarget webTarget = client.target(getBaseRestUri).path("deploy/" + blueprintsData.getBptoken());

        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("blueprint_token", blueprintsData.getBptoken());

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getStringAsFile(input),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .build("DELETE", Entity.entity(multipart, multipart.getMediaType()))
                .invoke();
        String message = response.readEntity(String.class);
        response.close();

        System.out.println(message);
        System.out.println(" Deployment Status for " + target + "," + blueprintsData.getBptoken());
        System.out.println(response);
        System.out.println();
    }

    public void configure(BuleprintsDataSet buleprintsDatas) {
        mapBM.clear();
        input = buleprintsDatas.getInput();
        for (BuleprintsData buleprintsData : buleprintsDatas.getBlueprints()) {
            String[] targets = buleprintsData.getTarget();
            for (String target : targets) {
                mapBM.put(target, buleprintsData);
            }
        }
    }
}

