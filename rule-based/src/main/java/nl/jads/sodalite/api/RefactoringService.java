package nl.jads.sodalite.api;

import com.fasterxml.jackson.databind.JsonNode;
import nl.jads.sodalite.dto.*;
import nl.jads.sodalite.events.*;
import nl.jads.sodalite.rules.RefactoringManager;
import nl.jads.sodalite.rules.RulesException;
import nl.jads.sodalite.scheduler.MonitoringDataCollector;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/api")
@Singleton
public class RefactoringService {
    private static final Logger log = Logger.getLogger(RefactoringService.class.getName());
    @Context
    ServletContext servletContext;

    private Map<String, RefactoringManager> managers = new HashMap<>();
    private MonitoringDataCollector monitoringDataCollector;

    public RefactoringService() {
//        managers.putIfAbsent("anyapp", new RefactoringManager());
        monitoringDataCollector = new MonitoringDataCollector();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appid}/events")
    public Response notify(@PathParam("appid") String appid, InputEventData inputEventData) {
        System.out.println("Received Message : " + inputEventData.toString());
        if ("LocationChanged".equals(inputEventData.getEventType())) {
            List<IEvent> iEventList = new ArrayList<>();
            iEventList.add(new LocationChangedEvent(
                    inputEventData.getPreviousLocation(), inputEventData.getNewLocation()));
            iEventList.add(new DeploymentChanged());
            return executeRules(appid, iEventList, inputEventData.getEventType());
        } else if ("DeploymentNeeded".equals(inputEventData.getEventType())) {
            List<IEvent> iEventList = new ArrayList<>();
            iEventList.add(new DeploymentNeeded(inputEventData.getNewLocation()));
            return executeRules(appid, iEventList, inputEventData.getEventType());
        } else if ("DeploymentRemove".equals(inputEventData.getEventType())) {
            List<IEvent> iEventList = new ArrayList<>();
            iEventList.add(new DeploymentRemove(inputEventData.getPreviousLocation()));
            return executeRules(appid, iEventList, inputEventData.getEventType());
        } else {
            return Response.serverError().entity("Unrecognized Event : " + inputEventData.getEventType()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appid}/r_events")
    public Response notify(@PathParam("appid") String appid, List<ResourceEvent> events) {
        System.out.println("Received events : " + events.size());
        List<IEvent> iEventList = new ArrayList<>();
        iEventList.addAll(events);
        return executeRules(appid, iEventList, "Resource Events");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appid}/r_event")
    public Response notify(@PathParam("appid") String appid, ResourceEvent event) {
        System.out.println("Received event : " + event.geteType());
        List<IEvent> iEventList = new ArrayList<>();
        iEventList.add(event);
        return executeRules(appid, iEventList, "Resource Event");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appid}/alerts")
    public Response notifyAlerts(@PathParam("appid") String appid, AlertsData alertsData) {
        System.out.println("Received An Alert : " + alertsData.toString());
        List<IEvent> iEvents = new ArrayList<>();
        for (AlertDTO alertDTO : alertsData.getAlertDTOS()) {
            Alert alert = new Alert();
            alert.setStatus(alertDTO.getStatus());
            Map map = (Map) alertDTO.getLabels();
            String alertname = (String) map.get("alertname");
            String instance = (String) map.get("instance");
            String severity = (String) map.get("severity");
            String os_id = (String) map.get("os_id");
            alert.setName(alertname);
            alert.setInstance(instance);
            alert.setSeverity(severity);
            alert.setVariant(os_id);
            iEvents.add(alert);
        }
        try {
            managers.get(appid).getPolicyExecutor().insertEvent(iEvents);
        } catch (RulesException e) {
            log.warning(e.getMessage());
            return Response.serverError().entity("Error Executing Refactoring Logic").build();
        }
        return Response.ok(alertsData.getVersion() + " Alert Received").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appid}/deployments")
    public Response createDeployment(@PathParam("appid") String appid, DeploymentInfo deploymentInfo) {
        System.out.println("Received the Information about the deployment  : " + deploymentInfo.getAadm_id());
        if (!managers.containsKey(appid)) {
            RefactoringManager manager = new RefactoringManager(appid);
            managers.put(appid, manager);
            try {
                manager.subscribeToPDS();
            } catch (Exception e) {
                log.warning(e.getMessage());
            }
        }
        managers.get(appid).setOriginalDeploymentInfo(deploymentInfo);
        try {
            managers.get(appid).loadDeployment(deploymentInfo.getAadm_id());
            log.info("Blueprint ID : " + deploymentInfo.getBlueprint_id());
            log.info("Deployment ID : " + deploymentInfo.getDeployment_id());
            log.info("Inputs : " + deploymentInfo.getInputs());
        } catch (Exception e) {
            return Response.serverError().entity("Error loading the AADM with id " +
                    deploymentInfo.getAadm_id() + " Error message is " + e.getMessage()).build();
        }
        return Response.ok("Information about the deployment with id " +
                deploymentInfo.getAadm_id() + " received").build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appid}/anyalerts")
    public Response notifyAnyAlerts(@PathParam("appid") String appid, JsonNode jsonNode) {
        System.out.println("Received An Alert : " + jsonNode.toString());
        return Response.ok("Alert Received").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/apps")
    public Response getAllDeployments() {
        List<DeploymentInfoOutput> outputs = new ArrayList<>();
        for (String appid : managers.keySet()) {
            RefactoringManager manager = managers.get(appid);
            DeploymentInfoOutput infoOutput = new DeploymentInfoOutput();
            infoOutput.setAppid(appid);
            infoOutput.setOriginal(manager.getOriginalDeploymentInfo());
            infoOutput.setCurrent(manager.getRefactoredDeploymentInfo());
            outputs.add(infoOutput);
        }
        return Response.ok(outputs).build();
    }


    private Response executeRules(String appid, List<IEvent> iEventList, String eventType) {
        try {
            managers.get(appid).getPolicyExecutor().insertEvent(iEventList);
        } catch (RulesException e) {
            log.warning(e.getMessage());
            return Response.serverError().entity("Error Executing Refactoring Logic").build();
        }
        return Response.ok(eventType + " Event Received").build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{appid}/variants")
    public Response updateVariantsInfo(@PathParam("appid") String appid, BuleprintsDataSet buleprintsDatas) {
        System.out.println("Received VariantInfos : " + buleprintsDatas.toString());
        managers.get(appid).configure(buleprintsDatas);
        return Response.ok("Updated VariantInfos").build();
    }

    @PUT
    @Path("/{appid}/rules")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@PathParam("appid") String appid,
                               @FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
        String fileLocation = "e://" + fileDetail.getFileName();
        String actualPath = servletContext.getRealPath("/WEB-INF/classes");
        System.out.println("Received a File :" + fileLocation);

        try (FileOutputStream out = new FileOutputStream(new File(actualPath + "/rules/refactoring.drl"))) {
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
        try {
            managers.get(appid).updatePolicyExecutor("refactoring.drl");
            return Response.ok("Rules updated").header("Access-Control-Allow-Origin", "*").
                    header("Access-Control-Allow-Origin", "POST").build();

        } catch (Exception e) {
            return Response.status(404).entity("No repository for the template").build();
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/monitoring/pull")
    public Response enableDisableMonitoring(@DefaultValue("disabled") @QueryParam("state") String state) {
        if ("enabled".equalsIgnoreCase(state)) {
            try {
                monitoringDataCollector.start();
                return Response.status(200).entity("Monitoring Enabled").build();
            } catch (Exception e) {
                log.warning(e.getMessage());
                return Response.status(500).entity(e.getMessage()).build();
            }
        } else {
            monitoringDataCollector.cancelTask();
            return Response.status(200).entity("Monitoring Disabled/Stopped").build();
        }

    }

    @PreDestroy
    public void destroy() {
        for (RefactoringManager manager : managers.values()) {
            manager.cleanUp();
        }
        monitoringDataCollector.shutdown();
    }
}
