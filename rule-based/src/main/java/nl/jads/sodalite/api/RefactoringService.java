package nl.jads.sodalite.api;

import nl.jads.sodalite.dto.AlertsData;
import nl.jads.sodalite.dto.BuleprintsDataSet;
import nl.jads.sodalite.dto.InputEventData;
import nl.jads.sodalite.events.*;
import nl.jads.sodalite.rules.RefactoringManager;
import nl.jads.sodalite.rules.RefactoringPolicyExecutor;
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
import java.util.List;

@Path("/api")
@Singleton
public class RefactoringService {
    @Context
    ServletContext servletContext;
    private RefactoringPolicyExecutor policyExecutor;
    private RefactoringManager refactoringManager;
    private MonitoringDataCollector monitoringDataCollector;

    public RefactoringService() {
        refactoringManager = new RefactoringManager();
        policyExecutor = new RefactoringPolicyExecutor("refactoring.drl", "rules/", refactoringManager);
        monitoringDataCollector = new MonitoringDataCollector();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/events")
    public Response notify(InputEventData inputEventData) {
        System.out.println("Received Message : " + inputEventData.toString());
        if ("LocationChanged".equals(inputEventData.getEventType())) {
            List<IEvent> iEventList = new ArrayList<>();
            iEventList.add(new LocationChangedEvent(
                    inputEventData.getPreviousLocation(), inputEventData.getNewLocation()));
            iEventList.add(new DeploymentChanged());
            return executeRules(iEventList, inputEventData.getEventType());
        } else if ("DeploymentNeeded".equals(inputEventData.getEventType())) {
            List<IEvent> iEventList = new ArrayList<>();
            iEventList.add(new DeploymentNeeded(inputEventData.getNewLocation()));
            return executeRules(iEventList, inputEventData.getEventType());
        } else if ("DeploymentRemove".equals(inputEventData.getEventType())) {
            List<IEvent> iEventList = new ArrayList<>();
            iEventList.add(new DeploymentRemove(inputEventData.getPreviousLocation()));
            return executeRules(iEventList, inputEventData.getEventType());
        } else {
            return Response.serverError().entity("Unrecognized Event : " + inputEventData.getEventType()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/alerts")
    public Response notifyAlerts(AlertsData alertsData) {
        System.out.println("Received An Alert : " + alertsData.toString());
        return Response.ok(alertsData.getVersion() + " Alert Received").build();
    }

    private Response executeRules(List<IEvent> iEventList, String eventType) {
        try {
            policyExecutor.insertEvent(iEventList);
        } catch (RulesException e) {
            e.printStackTrace();
            return Response.serverError().entity("Error Executing Refactoring Logic").build();
        }
        return Response.ok(eventType + " Event Received").build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/variants")
    public Response updateVariantsInfo(BuleprintsDataSet buleprintsDatas) {
        System.out.println("Received VariantInfos : " + buleprintsDatas.toString());
        refactoringManager.configure(buleprintsDatas);
        return Response.ok("Updated VariantInfos").build();
    }

    @PUT
    @Path("/rules")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
        String fileLocation = "e://" + fileDetail.getFileName();
        String actualPath = servletContext.getRealPath("/WEB-INF/classes");
        System.out.println("Received a File :" + fileLocation);
//        saving file
        try {
            FileOutputStream out = new FileOutputStream(new File(actualPath + "/rules/refactoring.drl"));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            policyExecutor.cleanUp();
            policyExecutor =
                    new RefactoringPolicyExecutor("refactoring.drl", "rules/", refactoringManager);
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
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Response.status(500).entity(e.getMessage()).build();
            }
        } else {
            monitoringDataCollector.shutdown();
            return Response.status(200).entity("Monitoring Disabled/Stopped").build();
        }

    }

    @PreDestroy
    public void destroy() {
        policyExecutor.cleanUp();
        monitoringDataCollector.shutdown();
    }
}
