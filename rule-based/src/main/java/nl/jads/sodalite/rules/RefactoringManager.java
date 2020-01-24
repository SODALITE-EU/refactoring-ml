package nl.jads.sodalite.rules;

import nl.jads.sodalite.dto.BlueprintMetadata;
import nl.jads.sodalite.dto.BuleprintsData;
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
            = "http://154.48.185.206:5000/";
    private Map<String, BuleprintsData> mapBM = new HashMap();
    private String currentBlueprintToken;

    public RefactoringManager() {
        loadConfig();
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
        BlueprintMetadata blueprintMetadata = blueprintsData.getBlueprint();
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(BASE_REST_URI).path("deploy/" + blueprintMetadata.getBlueprintToken());

        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("timestamp", blueprintMetadata.getTimestamp())
                        .field("version_id", String.valueOf(blueprintMetadata.getVersionId()));

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getResourceAsFile(blueprintsData.getInput()),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        String message = response.readEntity(String.class);
        response.close();

        System.out.println(message);
        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
        System.out.println(" Deployment Status for " + target + "," + blueprintMetadata.getBlueprintToken());
        System.out.println(response.getStatus());
        System.out.println(message);
        System.out.println();
        currentBlueprintToken = blueprintMetadata.getBlueprintToken();
    }

    public void undeployDeploymentModel(String target) {
        BuleprintsData blueprintsData = mapBM.get(target);
        BlueprintMetadata blueprintMetadata = blueprintsData.getBlueprint();
        ClientConfig config = new ClientConfig((MultiPartFeature.class));
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        Client client = ClientBuilder.newClient(config);
        WebTarget webTarget = client.target(BASE_REST_URI).path("deploy/" + blueprintMetadata.getBlueprintToken());

        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("timestamp", blueprintMetadata.getTimestamp())
                        .field("version_id", String.valueOf(blueprintMetadata.getVersionId()));

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getResourceAsFile(blueprintsData.getInput()),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        String message = response.readEntity(String.class);
        response.close();

        System.out.println(message);
        System.out.println(" Deployment Status for " + target + "," + blueprintMetadata.getBlueprintToken());
        System.out.println(response);
        System.out.println();
    }

    private void loadConfig() {
        BuleprintsData[] buleprintsDatas = POJOFactory.fromJsonFile("blueprintdata.json");
        for (BuleprintsData buleprintsData : buleprintsDatas) {
            String[] targets = buleprintsData.getTarget();
            for (String target : targets) {
                mapBM.put(target, buleprintsData);
            }
        }
    }
}

