package nl.jads.sodalite.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import kb.repository.KB;
import nl.jads.refactoringod.RefactoringOptionDiscovererKBApi;
import nl.jads.refactoringod.dto.FindNodeInput;
import nl.jads.sodalite.dto.*;
import nl.jads.sodalite.events.ResourceEvent;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import tosca.mapper.dto.Node;
import tosca.mapper.dto.Parameter;
import tosca.mapper.dto.Property;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private String pdsUri;
    private AADMModel aadm;
    private DeploymentInfo originalDeploymentInfo;
    private DeploymentInfo refactoredDeploymentInfo;
    private String token;
    private String graphdb;
    private RefactoringPolicyExecutor policyExecutor;
    RefactoringOptionDiscovererKBApi kbApi;
    private String namespace;
    private String refactorer;

    public RefactoringManager(String namespace) {
        this.namespace = namespace;
        xopera = System.getenv("xopera");
        if (xopera == null || "".equals(xopera.trim())) {
            xopera = BASE_REST_URI;
        }
        reasonerUri = System.getenv("reasoner");
        graphdb = System.getenv("graphdb");
        apikey = System.getenv("apikey");
        iacBuilderUri = System.getenv("iacbuilder");
        authUri = System.getenv("authapi");
        username = System.getenv("username");
        password = System.getenv("password");
        clientId = System.getenv("client_id");
        clientSecret = System.getenv("client_secret");
        pdsUri = System.getenv("pdsUri");
        refactorer = System.getenv("refactorer");

        BuleprintsDataSet buleprintsDatas = POJOFactory.fromJsonFile("blueprintdata.json");
        if (buleprintsDatas != null) {
            configure(buleprintsDatas);
        }
        policyExecutor = new RefactoringPolicyExecutor("refactoring.drl", "rules/", this);
        initKB();
    }

    public void initKB() {
        if (kbApi != null) {
            return;
        }
        try {
            kbApi = new RefactoringOptionDiscovererKBApi(new KB(graphdb, KB.REPOSITORY));
        } catch (Exception e) {
            log.warn(e);
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

    public void loadOriginalDeployment() throws Exception {
        loadDeployment(originalDeploymentInfo.getAadm_id(), originalDeploymentInfo.getVersion());
    }

    public void loadRefactoredDeployment() throws Exception {
        loadDeployment(refactoredDeploymentInfo.getAadm_id(), refactoredDeploymentInfo.getVersion());
    }

    public void loadDeployment(String aadmId, String version) throws Exception {
        aadm = AADMModelBuilder.fromJsonText(getDeploymentSimple(aadmId, version));
        aadm.setId(aadmId);
        aadm.updateNodeTypes();
        System.out.println("AADM runtime model was loaded : " + aadmId);
    }

    public Node getNodeFromKB(String nodeUri) throws ParseException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(reasonerUri).path("nodeFull").queryParam("resource", nodeUri);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Invocation invocation =
                builder.buildGet();
        Response response = invocation.invoke();
        System.out.println(response.getStatus());
        String nodeJson = response.readEntity(String.class);
        response.close();
        JSONObject json = (JSONObject) new JSONParser().parse(nodeJson);
        return AADMModelBuilder.toNode(json, nodeUri);
    }

    public String getDeploymentSimple(String aadmId, String version) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(reasonerUri).path("aadm").queryParam("aadmIRI", aadmId).
                        queryParam("refactorer", true);
        if (version != null && !version.isEmpty()) {
            webTarget = webTarget.queryParam("version", version);
        }
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

    public JsonObject getCompleteDeploymentModel(String aadmId, String version) throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(reasonerUri).path("aadm")
                        .queryParam("aadmIRI", aadmId);

        if (version != null && !version.isEmpty()) {
            webTarget = webTarget.queryParam("version", version);
        }
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        Invocation invocation =
                builder.buildGet();
        Response response = invocation.invoke();
        System.out.println(response.getStatus());
        String aadmJson = response.readEntity(String.class);
        response.close();
        System.out.println("AADM JSON was retrieved : " + aadmId);
        return new Gson().fromJson(aadmJson, JsonObject.class);
    }

    public void saveAndUpdate() throws Exception {
        saveDeploymentModelInKB();
        buildIaCForCurrentDeployment();
        updateCurrentDeployment();
    }

    public void subscribeToPDS() throws Exception {
//        if (token == null) {
//            refreshSecurityToken();
//        }
        Client client = ClientBuilder.newClient();
        WebTarget webTarget =
                client.target(pdsUri).path("subscribe");
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        if (apikey != null) {
            builder.header("X-API-Key", apikey);
        }
        SubscribeRequest request = new SubscribeRequest();
        request.setNamespace(namespace);
        request.setEndpoint(refactorer + namespace + "/r_events");
        ResourceEvent resourceEvent = new ResourceEvent();
        resourceEvent.setrType("any");
        resourceEvent.seteType("KBUpdated");
        List<ResourceEvent> resourceEvents = new ArrayList<>();
        resourceEvents.add(resourceEvent);
        ResourceEvent[] arr = new ResourceEvent[resourceEvents.size()];
        request.setPayload(resourceEvents.toArray(arr));
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        String jsonString = gson.toJson(request);

        Invocation invocation =
                builder.buildPost(Entity.json(jsonString));
        try {
            Response response = invocation.invoke();
            System.out.println(response.getStatus());
            String result = response.readEntity(String.class);
            response.close();
            System.out.println("Subscribed to PDS : " + result);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                refreshSecurityToken();
            } else {
                throw ex;
            }
        }
    }

    public void saveDeploymentModelInKB() throws Exception {
        if (token == null) {
            refreshSecurityToken();
        }
        Client client = ClientBuilder.newClient();
        String aadmURI = aadm.getId();
//        if (!aadmURI.endsWith("refac")) {
//            aadmURI = aadmURI + "refac";
//            aadm.setId(aadmURI);
//            refactoredDeploymentInfo.setAadm_id(aadmURI);
//        }
        WebTarget webTarget =
                client.target(reasonerUri).path("saveAADM");
        Form form = new Form();
        form.param("aadmTTL", aadm.getExchangeAADM());
        form.param("token", token);
        form.param("aadmURI", aadmURI);
        form.param("version", aadm.getVersion());
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
            response.close();
            JsonObject jsonObject = new Gson().fromJson(result, JsonObject.class);
            System.out.println(jsonObject.toString());
            String aadmuri = jsonObject.get("aadmuri").getAsString();
            aadm.setId(aadmuri);
            refactoredDeploymentInfo.setAadm_id(aadmURI);
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
        buildIaC(aadm.getId(), aadm.getNamespace(), aadm.getVersion());
    }

    public Node findMatchingNodeFromRM(String expr) throws ParseException {
        return findMatchingNode(expr, originalDeploymentInfo.getAadm_id());
    }

    public Node findMatchingNodeFromDM(String expr) throws ParseException {
        return findMatchingNode(expr, refactoredDeploymentInfo.getAadm_id());
    }

    public Node findMatchingNode(String expr, String aadm) throws ParseException {
        Pattern pattern = Pattern.compile("\\?\\w+");
        List<String> vars = new ArrayList<>();
        Matcher matcher = pattern.matcher(expr);
        while (matcher.find()) {
            vars.add(matcher.group().substring(1));
        }
        FindNodeInput findNodeInput = new FindNodeInput();
        findNodeInput.setExpr(expr);
        findNodeInput.setVars(vars);
        findNodeInput.setAadm(aadm);
        Set<kb.dto.Node> nodes = kbApi.getComputeNodeInstances(findNodeInput);
        System.out.println("Found " + nodes.size() + " node matching the expression : " + expr);
        if (!nodes.isEmpty()) {
            String[] arrs = ((kb.dto.Node) nodes.toArray()[0]).getUri().split("/");
            String nodeUri = arrs[arrs.length - 2] + "/" + arrs[arrs.length - 1];
            return getNodeFromKB(nodeUri);
        }
        return null;
    }

    public List<Node> getNodeMatchingReqFromRM(String expr) throws ParseException {
        return getNodeMatchingReq(expr, originalDeploymentInfo.getAadm_id());
    }

    public List<Node> getNodeMatchingReqFromDM(String expr) throws ParseException {
        return getNodeMatchingReq(expr, refactoredDeploymentInfo.getAadm_id());
    }

    public List<Node> getNodeMatchingReq(String expr, String aadm) throws ParseException {
        Set<kb.dto.Node> nodes = kbApi.getNodeMatchingReq(expr, aadm);
        System.out.println("Found " + nodes.size() + " node matching the expression : " + expr);
        List<Node> nodeList = new ArrayList<>();
        for (kb.dto.Node nodeKB : nodes) {
            String[] arrs = nodeKB.getUri().split("/");
            String nodeUri = arrs[arrs.length - 2] + "/" + arrs[arrs.length - 1];
            nodeList.add(getNodeFromKB(nodeUri));
        }
        return nodeList;
    }


    public void buildIaC(String aadmId, String nameSpace, String version) throws Exception {
        JsonObject data = getCompleteDeploymentModel(aadmId, version);
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
            refactoredDeploymentInfo.setBlueprint_id
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
        deploy(refactoredDeploymentInfo.getBlueprint_id(), refactoredDeploymentInfo.getUpdatedInput());
    }

    public void deploy(String bpToken, String inputFile) {
        System.out.println("Deploying : " + bpToken);
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(xopera).path("deployment/deploy")
                .queryParam("blueprint_id", bpToken).queryParam("workers", 1);

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
        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
        System.out.println(message);
        JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
        refactoredDeploymentInfo.setDeployment_id(jsonObject.get("deployment_id").getAsString());
    }

    public void updateCurrentDeployment() {
        update(refactoredDeploymentInfo.getDeployment_id(),
                refactoredDeploymentInfo.getBlueprint_id(),
                refactoredDeploymentInfo.getUpdatedInput());
    }

    public void update(String dpId, String bpToken, String inputFile) {
        System.out.println("Updating : " + dpId + " to :" + bpToken);
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();
        WebTarget webTarget = client.target(xopera).path("deployment/" + dpId + "/update")
                .queryParam("blueprint_id", bpToken).queryParam("workers", 1);

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

        System.out.println(response.getStatus() + " "
                + response.getStatusInfo() + " " + response);
        System.out.println(message);
        JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
        refactoredDeploymentInfo.setDeployment_id(jsonObject.get("deployment_id").getAsString());
    }

    public void undeployDeploymentModel(String target) {
        BuleprintsData blueprintsData = mapBM.get(target);
        unDeploy(blueprintsData.getBptoken(), input);
    }

    public void unDeployCurrentDeployment() {
        unDeploy(refactoredDeploymentInfo.getDeployment_id(), refactoredDeploymentInfo.getUpdatedInput());
    }

    public void unDeploy(String dpId, String inputFile) {
        System.out.println("Undeploying : " + dpId);
        ClientConfig config = new ClientConfig((MultiPartFeature.class));
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        Client client = ClientBuilder.newClient(config);
        WebTarget webTarget =
                client.target(xopera).path("deployment/" + dpId + "/undeploy")
                        .queryParam("workers", 1);
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
                .buildPost(Entity.entity(multipart, multipart.getMediaType()))
                .invoke();
        String message = response.readEntity(String.class);
        response.close();
        System.out.println(message);
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

    public DeploymentInfo getOriginalDeploymentInfo() {
        return originalDeploymentInfo;
    }

    public void setOriginalDeploymentInfo(DeploymentInfo originalDeploymentInfo) {
        this.originalDeploymentInfo = originalDeploymentInfo;
        this.refactoredDeploymentInfo = new DeploymentInfo();
        this.refactoredDeploymentInfo.setDeployment_id(originalDeploymentInfo.getDeployment_id());
        this.refactoredDeploymentInfo.setAadm_id(originalDeploymentInfo.getAadm_id());
        this.refactoredDeploymentInfo.setBlueprint_id(originalDeploymentInfo.getBlueprint_id());
        this.refactoredDeploymentInfo.setInput(originalDeploymentInfo.getInputs());
        this.refactoredDeploymentInfo.setVersion(originalDeploymentInfo.getVersion());
    }

    public DeploymentInfo getRefactoredDeploymentInfo() {
        return refactoredDeploymentInfo;
    }

    public void setRefactoredDeploymentInfo(DeploymentInfo refactoredDeploymentInfo) {
        this.refactoredDeploymentInfo = refactoredDeploymentInfo;
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

    public RefactoringPolicyExecutor getPolicyExecutor() {
        return policyExecutor;
    }

    public void updatePolicyExecutor(String ruleFile) {
        policyExecutor.cleanUp();
        policyExecutor = new RefactoringPolicyExecutor(ruleFile, "rules/", this);
    }

    public void cleanUp() {
        policyExecutor.cleanUp();
    }

    public String getGraphdb() {
        return graphdb;
    }

    public void setGraphdb(String graphdb) {
        this.graphdb = graphdb;
    }

    public tosca.mapper.dto.Property createProperty(String name, String value) {
        Property property = new Property(name);
        if (value.contains(DTOConstraints.GET_INPUT)) {
            String value1 = value.split(DTOConstraints.GET_INPUT)[1].trim();
            Set<Parameter> parameters = new HashSet<>();
            Parameter parameter = new Parameter(DTOConstraints.GET_INPUT);
            parameter.setValue(value1);
            parameters.add(parameter);
            property.setParameters(parameters);
        } else {
            property.setValue(value);
        }
        return property;
    }

    public tosca.mapper.dto.Requirement createRequirement(String name, String value) {
        tosca.mapper.dto.Requirement property = new tosca.mapper.dto.Requirement(name);
        String[] values = value.split(":");
        Set<Parameter> parameters = new HashSet<>();
        Parameter parameter = new Parameter(values[0].trim());
        parameter.setValue(values[1].trim());
        parameters.add(parameter);
        property.setParameters(parameters);
        return property;
    }

    public String getPdsUri() {
        return pdsUri;
    }

    public void setPdsUri(String pdsUri) {
        this.pdsUri = pdsUri;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setRefactorer(String refactorer) {
        this.refactorer = refactorer;
    }
}

