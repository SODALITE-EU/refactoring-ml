package nl.jads.sodalite.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class DisabledRuleSet {
    private List<String> disabledRules = new ArrayList<String>();

    public List<String> getDisabledRules() {
        return disabledRules;
    }

    public synchronized void disable(String rule) {
        disabledRules.add(rule);
    }

    public synchronized boolean contains(String rule) {
        return disabledRules.contains(rule);
    }

}
