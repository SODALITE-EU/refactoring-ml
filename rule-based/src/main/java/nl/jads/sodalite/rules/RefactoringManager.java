package nl.jads.sodalite.rules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nl.jads.sodalite.dto.*;
import nl.jads.sodalite.utils.AADMModelBuilder;
import nl.jads.sodalite.utils.POJOFactory;
import nl.jads.sodalite.utils.ResourceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RefactoringManager {
    private static final String BASE_REST_URI
            = "http://154.48.185.209:5000/";
    private static final Logger log = LogManager.getLogger();
    private Map<String, BuleprintsData> mapBM = new HashMap<String, BuleprintsData>();
    private String xopera;
    private String reasonerUri;
    private String iacBuilderUri;
    private String authUri;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String input;
    private String apikey;
    private AADMModel aadm;
    private DeploymentInfo currentDeploymentInfo;
    private DeploymentInfo nextDeploymentInfo;
    private String token;

    public RefactoringManager() {
        xopera = System.getenv("xopera");
        if (xopera == null || "".equals(xopera.trim())) {
            xopera = BASE_REST_URI;
        }
        reasonerUri = System.getenv("reasoner");
        apikey = System.getenv("apikey");
        iacBuilderUri = System.getenv("iacbuilder");
        authUri = System.getenv("authapi");
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

    public void loadCurrentDeployment() throws Exception {
        loadDeployment(currentDeploymentInfo.getAadm_id());
    }

    public void loadDeployment(String aadmId) throws Exception {
        aadm = AADMModelBuilder.fromJsonText(getDeploymentSimple(aadmId));
        aadm.setId(aadmId);
        aadm.updateNodeTypes();
        System.out.println("AADM runtime model was loaded : " + aadmId);

    }

    public String getDeploymentSimple(String aadmId) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(reasonerUri).path("aadm").queryParam("aadmIRI", aadmId)
                        .queryParam("refactorer", true);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Invocation invocation =
                builder.buildGet();
        Response response = invocation.invoke();
        System.out.println(response.getStatus());
        String aadmJson = response.readEntity(String.class);
        System.out.println(aadmJson);
        response.close();
        return aadmJson;
    }

    public JsonObject getCompleteDeploymentModel(String aadmId) throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(reasonerUri).path("aadm").queryParam("aadmIRI", aadmId);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Invocation invocation =
                builder.buildGet();
        Response response = invocation.invoke();
        System.out.println(response.getStatus());
        String aadmJson = response.readEntity(String.class);
        System.out.println(aadmJson);
        response.close();
        System.out.println("AADM JSON was retrieved : " + aadmId);
        return new Gson().fromJson(aadmJson, JsonObject.class);
    }

    public void saveDeploymentModelInKB() throws Exception {
        if (token == null) {
            refreshSecurityToken();
        }
        Client client = ClientBuilder.newClient();
        String aadmURI = aadm.getId();
        if (!aadmURI.endsWith("refac")) {
            aadmURI = aadmURI + "refac";
            aadm.setId(aadmURI);
            currentDeploymentInfo.setAadm_id(aadmURI);
        }
        WebTarget webTarget =
                client.target(reasonerUri).path("saveAADM");
        Form form = new Form();
        form.param("aadmTTL", aadm.getExchangeAADM());
        form.param("token", token);
        form.param("aadmURI", aadmURI);
        form.param("namespace", aadm.getNamespace());

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Invocation invocation =
                builder.buildPost(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        try {
            Response response = invocation.invoke();
            System.out.println(response.getStatus());
            String result = response.readEntity(String.class);
            System.out.println(result);
            response.close();
            JsonObject jsonObject = new Gson().fromJson(result, JsonObject.class);
            String aadmuri = jsonObject.get("aadmuri").getAsString();
            aadm.setId(aadmuri);
            currentDeploymentInfo.setAadm_id(aadmURI);
            System.out.println("AADM was saved : " + aadmuri);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                refreshSecurityToken();
            } else {
                throw ex;
            }
        }
    }

    public void buildIaCForCurrentDeployment() throws Exception {
        buildIaC(aadm.getId(), aadm.getNamespace());
    }

    public void buildIaC(String aadmId, String nameSpace) throws Exception {
        JsonObject data = getCompleteDeploymentModel(aadmId);
        IaCBuilderInput input = new IaCBuilderInput();
        input.setName(nameSpace);
        input.setData(data);
        Gson gson = new Gson();
        String jsonInput = gson.toJson(input);
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(iacBuilderUri).path("parse");
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Invocation invocation =
                builder.buildPost(Entity.entity(jsonInput, MediaType.APPLICATION_JSON_TYPE));
        try {
            Response response = invocation.invoke();
            System.out.println(response.getStatus());
            String result = response.readEntity(String.class);
            System.out.println(result);
            response.close();
            JsonObject jsonObject = new Gson().fromJson(result, JsonObject.class);
            currentDeploymentInfo.setBlueprint_token
                    (jsonObject.get("blueprint_id").getAsString());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                refreshSecurityToken();
            } else {
                throw ex;
            }
        }
    }

    public void deployCurrentDeployment() {
        deploy(currentDeploymentInfo.getBlueprint_token(), currentDeploymentInfo.getUpdatedInput());
    }

    public void deploy(String bpToken, String inputFile) {
        System.out.println("Deploying : " + bpToken);
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(xopera).path("deployment/deploy")
                .queryParam("blueprint_id", bpToken);

        FormDataMultiPart multipart =
                new FormDataMultiPart();
        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getStringAsFile(inputFile),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Invocation.Builder builder = webTarget.request(MediaType.MULTIPART_FORM_DATA_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Response response = builder.buildPost(
                Entity.entity(multipart, multipart.getMediaType())).invoke();
        String message = response.readEntity(String.class);
        response.close();

        System.out.println(message);
        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
        System.out.println(response.getStatus());
        System.out.println(message);
        System.out.println();
        JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
        currentDeploymentInfo.setDeployment_id(jsonObject.get("deployment_id").getAsString());
    }

    public void update(String dpId, String bpToken, String inputFile) {
        System.out.println("Updating : " + dpId + " to :" + bpToken);
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(xopera).path("deployment/" + dpId + "/update")
                .queryParam("blueprint_id", bpToken);

        FormDataMultiPart multipart = new FormDataMultiPart();

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getStringAsFile(inputFile),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Response response =
                builder.buildPost(Entity.entity(multipart, multipart.getMediaType())).invoke();
        String message = response.readEntity(String.class);
        response.close();

        System.out.println(message);
        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
        System.out.println(response.getStatus());
        System.out.println(message);
        System.out.println();
        JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
        currentDeploymentInfo.setDeployment_id(jsonObject.get("deployment_id").getAsString());
    }

    public void undeployDeploymentModel(String target) {
        BuleprintsData blueprintsData = mapBM.get(target);
        unDeploy(blueprintsData.getBptoken(), input);
    }

    public void unDeploy(String dpId, String inputFile) {
        System.out.println("Undeploying : " + dpId);
        ClientConfig config = new ClientConfig((MultiPartFeature.class));
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        Client client = ClientBuilder.newClient(config);
        WebTarget webTarget = client.target(xopera).path("deployment/" + dpId + "/undeploy");
        FormDataMultiPart multipart =
                new FormDataMultiPart();

        FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("inputs_file",
                        ResourceUtil.getStringAsFile(inputFile),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

        multipart.bodyPart(fileDataBodyPart);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Response response = builder
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

    public AADMModel getAadm() {
        return aadm;
    }

    public void setAadm(AADMModel aadm) {
        this.aadm = aadm;
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

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public DeploymentInfo getCurrentDeploymentInfo() {
        return currentDeploymentInfo;
    }

    public void setCurrentDeploymentInfo(DeploymentInfo currentDeploymentInfo) {
        this.currentDeploymentInfo = currentDeploymentInfo;
    }

    public DeploymentInfo getNextDeploymentInfo() {
        return nextDeploymentInfo;
    }

    public void setNextDeploymentInfo(DeploymentInfo nextDeploymentInfo) {
        this.nextDeploymentInfo = nextDeploymentInfo;
    }

    private void refreshSecurityToken() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(authUri).path("auth/realms/SODALITE/protocol/openid-connect/token");
        Form form = new Form();
        form.param("grant_type", "password");
        form.param("client_id", clientId);
        form.param("client_secret", clientSecret);
        form.param("password", password);
        form.param("username", username);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Invocation invocation =
                builder.buildPost(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        Response response = invocation.invoke();
        System.out.println(response.getStatus());
        String result = response.readEntity(String.class);
        System.out.println(result);
        JsonObject jsonObject = new Gson().fromJson(result, JsonObject.class);
        token = jsonObject.get("access_token").getAsString();
    }

    public String getXopera() {
        return xopera;
    }

    public void setXopera(String xopera) {
        this.xopera = xopera;
    }

}

