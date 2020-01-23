package nl.jads.sodalite.rules;

import nl.jads.sodalite.dto.BlueprintMetadata;
import nl.jads.sodalite.dto.BuleprintsData;
import nl.jads.sodalite.dto.POJOFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RefactoringManager {
    private static final String BASE_REST_URI
            = "http://154.48.185.206:5000/";
    private Map<String, BlueprintMetadata> mapBM = new HashMap();
    private Map<String, String> keyToToken = new HashMap<>();
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

    public void addBlueprintMetadata(BlueprintMetadata bm) {
        mapBM.put(bm.getBlueprintToken(), bm);
    }

    public BlueprintMetadata getBlueprintMetadata(String token) {
        return mapBM.get(token);
    }

    public Collection<BlueprintMetadata> getAllBlueprintMetadata() {
        return mapBM.values();
    }

    public Set<String> getAllTokens() {
        return mapBM.keySet();
    }

    public void addDeploymentModelMapping(String target, String token) {
        keyToToken.put(target, token);
    }

    public String getDeploymentModel(String target) {
        return keyToToken.get(target);
    }

    public void deployDeploymentModel(String target) {
        String token = getDeploymentModel(target);
        BlueprintMetadata blueprintMetadata = getBlueprintMetadata(token);
        Client client = ClientBuilder.newClient();
        Response response = client
                .target(BASE_REST_URI)
                .path("deploy/" + token)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(blueprintMetadata, MediaType.APPLICATION_JSON));
        String message = response.readEntity(String.class);
        response.close();
        System.out.println(" Deployment Status for " + target + "," + token);
        System.out.println(response.getStatus());
        System.out.println(message);
        System.out.println();
        currentBlueprintToken = token;
    }

    public void undeployDeploymentModel(String target) {
        String token = getDeploymentModel(target);
        BlueprintMetadata blueprintMetadata = getBlueprintMetadata(token);
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        Client client = ClientBuilder.newClient(config);

        String response = client.target(BASE_REST_URI)
                .path("deploy/" + token)
                .request(MediaType.APPLICATION_JSON)
                .build("DELETE", Entity.entity(blueprintMetadata, MediaType.APPLICATION_JSON))
                .invoke(String.class);

        System.out.println(response);
        System.out.println(" Deployment Status for " + target + "," + token);
        System.out.println(response);
        System.out.println();
    }

    private void loadConfig() {
        BuleprintsData[] buleprintsDatas = POJOFactory.fromJsonFile("blueprintdata.json");
        for (BuleprintsData buleprintsData : buleprintsDatas) {
            String[] targets = buleprintsData.getTarget();
            addBlueprintMetadata(buleprintsData.getBlueprint());
            for (String target : targets) {
                addDeploymentModelMapping(target, buleprintsData.getBlueprint().getBlueprintToken());
            }
        }
    }
}

