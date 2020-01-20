package nl.jads.sodalite.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 */
public class RegulationRuleSet {

    private List<String> ruleSet = new ArrayList<String>();

    public void addRuleName(String name) {
        ruleSet.add(name);
    }

    public void addRuleNames(String names) {
        names = names.trim();
        if (names.indexOf(",") > 0) {
            String[] nameList = names.split(",");
            Collections.addAll(ruleSet, nameList);
        } else {
            ruleSet.add(names);
        }
    }

    public boolean contains(String name) {
        return ruleSet.contains(name);
    }

    public List<String> getAllRules() {
        return ruleSet;
    }

    public String toString() {
        return ruleSet.toString();
    }
}
