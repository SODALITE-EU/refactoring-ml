package nl.jads.sodalite.rules;

import nl.jads.sodalite.dto.BuleprintsData;
import nl.jads.sodalite.dto.BuleprintsDataSet;
import nl.jads.sodalite.dto.DeploymentModel;
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
    private String reasonerUri;
    private String iacBuilderUri;
    private String authUri;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String input;
    private DeploymentModel deploymentModel;

    public RefactoringManager() {
        getBaseRestUri = System.getenv("xopera");
        if (getBaseRestUri == null || "".equals(getBaseRestUri.trim())) {
            getBaseRestUri = BASE_REST_URI;
        }
        reasonerUri = System.getenv("reasoner");
        iacBuilderUri = System.getenv("iacbuilder");
        authUri = System.getenv("authUri");
        username = System.getenv("username");
        password = System.getenv("password");
        clientId = System.getenv("client_id");
        clientSecret = System.getenv("client_secret");

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
        deploy(blueprintsData.getBptoken(), input);
    }

    public void deploy(String bpToken, String inputFile) {
        System.out.println("Deploying : " + bpToken);
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(getBaseRestUri).path("deploy/" + bpToken);

        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("blueprint_token", bpToken);

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getStringAsFile(inputFile),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multipart, multipart.getMediaType()));
        String message = response.readEntity(String.class);
        response.close();

        System.out.println(message);
        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
        System.out.println(response.getStatus());
        System.out.println(message);
        System.out.println();
        currentBlueprintToken = bpToken;
    }

    public void undeployDeploymentModel(String target) {
        BuleprintsData blueprintsData = mapBM.get(target);
        unDeploy(blueprintsData.getBptoken(), input);
    }

    public void unDeploy(String bpToken, String inputFile) {
        System.out.println("Undeploying : " + bpToken);
        ClientConfig config = new ClientConfig((MultiPartFeature.class));
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        Client client = ClientBuilder.newClient(config);
        WebTarget webTarget = client.target(getBaseRestUri).path("deploy/" + bpToken);

        FormDataMultiPart multipart =
                new FormDataMultiPart()
                        .field("blueprint_token", bpToken);

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getStringAsFile(inputFile),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .build("DELETE", Entity.entity(multipart, multipart.getMediaType()))
                .invoke();
        String message = response.readEntity(String.class);
        response.close();
        System.out.println(message);
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

    public DeploymentModel getDeploymentModel() {
        return deploymentModel;
    }

    public void setDeploymentModel(DeploymentModel deploymentModel) {
        this.deploymentModel = deploymentModel;
    }

    public String getReasonerUri() {
        return reasonerUri;
    }

    public void setReasonerUri(String reasonerUri) {
        this.reasonerUri = reasonerUri;
    }

    public String getIacBuilderUri() {
        return iacBuilderUri;
    }

    public void setIacBuilderUri(String iacBuilderUri) {
        this.iacBuilderUri = iacBuilderUri;
    }

    public String getAuthUri() {
        return authUri;
    }

    public void setAuthUri(String authUri) {
        this.authUri = authUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}

