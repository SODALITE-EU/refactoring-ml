package nl.jads.sodalite.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO
 */
public class EventCollection implements IEvent {
    private final List<IEvent> iEvents = new ArrayList<IEvent>();
    private DisabledRuleSet disabledRuleSet;

    public void addIEvent(IEvent iEvent) {
        iEvents.add(iEvent);
    }

    public void addAllIEvents(Collection<IEvent> events) {
        iEvents.addAll(events);
    }

    public List<IEvent> getiEvents() {
        return iEvents;
    }

    public DisabledRuleSet getDisabledRuleSet() {
        return disabledRuleSet;
    }

    public void setDisabledRuleSet(DisabledRuleSet disabledRuleSet) {
        this.disabledRuleSet = disabledRuleSet;
    }

}
