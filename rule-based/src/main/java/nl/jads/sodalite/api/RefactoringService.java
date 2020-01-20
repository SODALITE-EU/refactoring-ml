package nl.jads.sodalite.api;

import nl.jads.sodalite.dto.InputEventData;
import nl.jads.sodalite.events.IEvent;
import nl.jads.sodalite.events.LocationChangedEvent;
import nl.jads.sodalite.rules.RefactoringPolicyExecutor;
import nl.jads.sodalite.rules.RuleEngineSingleton;
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

    public RefactoringService() {
        policyExecutor = new RefactoringPolicyExecutor("refactoring.drl", "rules/");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/inputs")
    public Response notify(InputEventData inputEventData) {
        if ("LocationChanged".equals(inputEventData.getEventType())) {
            LocationChangedEvent event =
                    new LocationChangedEvent(inputEventData.getPreviousLocation(), inputEventData.getNewLocation());
            List<IEvent> iEventList = new ArrayList<>();
            iEventList.add(event);
            try {
               policyExecutor.insertEvent(iEventList);
            } catch (RulesException e) {
                e.printStackTrace();
                return Response.serverError().entity("Error Executing Refactoring Logic").build();
            }
        }
        return Response.ok("Event Received").build();
    }
}
