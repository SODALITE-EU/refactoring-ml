package nl.jads.sodalite.api;

import nl.jads.sodalite.dto.InputEventData;
import nl.jads.sodalite.events.*;
import nl.jads.sodalite.rules.RefactoringManager;
import nl.jads.sodalite.rules.RefactoringPolicyExecutor;
import nl.jads.sodalite.rules.RulesException;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/events")
@Singleton
public class RefactoringService {
    private RefactoringPolicyExecutor policyExecutor;
    private RefactoringManager refactoringManager;

    public RefactoringService() {
        refactoringManager = new RefactoringManager();
        policyExecutor = new RefactoringPolicyExecutor("refactoring.drl", "rules/", refactoringManager);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/inputs")
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

    private Response executeRules(List<IEvent> iEventList, String eventType) {
        try {
            policyExecutor.insertEvent(iEventList);
        } catch (RulesException e) {
            e.printStackTrace();
            return Response.serverError().entity("Error Executing Refactoring Logic").build();
        }
        return Response.ok(eventType + " Event Received").build();
    }
}
