package nl.jads.sodalite.rules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * TODO documentation
 */
public abstract class DroolsRules {

    private static final Logger log = LogManager.getLogger();
    protected KieContainer kieContainer;
    protected String ruleDir;
    private String ruleFile1;
    private Map<String, RegulationRule> regulationRuleMap = new HashMap<String, RegulationRule>();

    protected DroolsRules(String ruleFile, String ruleDir) {
        this();
        this.ruleFile1 = ruleFile;
        this.ruleDir = ruleDir;
        addRule1(ruleFile);
    }

    protected DroolsRules(DataHandler ruleFileBinary) {
        this();
        try {
            addRulesFromBinary(ruleFileBinary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected DroolsRules() {
    }

    public DroolsRules(String ruleDir) {
        this();
        this.ruleDir = ruleDir;
    }


    public String getRuleFile() {
        return ruleFile1;
    }

    public boolean addRule(String ruleFile) {
        if (ruleFile1 == null) {
            ruleFile1 = ruleFile;
        }
        return addRule1(ruleFile);
    }

    private boolean addRule1(String ruleFile) {
        if (log.isInfoEnabled()) {
            log.info("Reading KB from " + ruleFile);
        }
        Resource resource;
        String path;
        if (new File(ruleFile).exists()) {
            resource = ResourceFactory.newFileResource(ruleFile);
            path = ruleFile;
        } else {
            path = ruleDir + ruleFile;
            if (new File(path).exists()) {
                resource = ResourceFactory.newFileResource(path);
            } else {
                resource = ResourceFactory.newClassPathResource(path, DroolsRules.class);
            }
        }
        resource.setResourceType(ResourceType.DRL);
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        ReleaseId relId = kieServices.newReleaseId(ruleFile, "road-rep", "1.0.0"); // To create a unique identifier
        kieFileSystem.generateAndWritePomXML(relId);
        kieFileSystem.write(path, resource);
        // Create the builder for the resources of the File System
        KieBuilder kbuilder = kieServices.newKieBuilder(kieFileSystem);
        // Build the Kie Bases
        kbuilder.buildAll();
        // Check for errors

        if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
            System.out.println("Error found in rule file: " + ruleFile
                    + " Errors found: " + kbuilder.getResults().getMessages());
            return false;
        }
//        System.out.println(kbuilder.getKieModule().getReleaseId());
        // Get the Release ID (mvn style: groupId, artifactId,version)
        Collection<InternalKnowledgePackage> knowledgeBaseCollection;
        if (kieContainer == null) {
            kieContainer = kieServices.newKieContainer(relId);
            knowledgeBaseCollection = ((InternalKnowledgeBase) kieContainer.getKieBase()).getPackagesMap().values();
        } else {
            KieContainer tempKieContainer = kieServices.newKieContainer(relId);
            knowledgeBaseCollection = ((InternalKnowledgeBase) tempKieContainer.getKieBase()).getPackagesMap().values();
            ((InternalKnowledgeBase) kieContainer.getKieBase()).addPackages(knowledgeBaseCollection);
        }
        for (KiePackage kp : knowledgeBaseCollection) {
            for (Rule rule : kp.getRules()) {
                regulationRuleMap.put(rule.getName(), new RegulationRule(rule.getName()));
            }
        }
        return true;
    }

    public List<RegulationRule> getRules(String ruleFile) {

        // Loading all rules from a rules file
        Resource resource = ResourceFactory.newFileResource(ruleDir + ruleFile);
        // Resource resource = ResourceFactory.newClassPathResource( ruleFile,
        // DroolsRulesImpl.class);
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(resource.getSourcePath(), resource);
        // Create the builder for the resources of the File System
        KieBuilder kbuilder = kieServices.newKieBuilder(kieFileSystem);
        // Build the Kie Bases
        kbuilder.buildAll();
        // Check for errors
        if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
            log.error("Error found in rule file: " + ruleFile
                    + " Errors found: " + kbuilder.getResults().getMessages());
            return new ArrayList<RegulationRule>();
        }
        // Get the Release ID (mvn style: groupId, artifactId,version)
        ReleaseId relId = kbuilder.getKieModule().getReleaseId();
        Collection<KiePackage> knowledgeBaseCollection = kieServices.newKieContainer(relId).getKieBase().getKiePackages();
        List<RegulationRule> regulationRules = new ArrayList<RegulationRule>();
        for (KiePackage kp : knowledgeBaseCollection) {
            for (Rule rule : kp.getRules()) {
                regulationRules.add(new RegulationRule(rule.getName()));
            }
        }
        return regulationRules;
    }

    private boolean addRulesFromBinary(DataHandler dataHandler) throws IOException {
        final InputStream in = dataHandler.getInputStream();
        byte[] byteArray = org.apache.commons.io.IOUtils.toByteArray(in);
        // Loading all rules from a rules file
        Resource resource = ResourceFactory.newByteArrayResource(byteArray);
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(resource.getSourcePath(), resource);
        // Create the builder for the resources of the File System
        KieBuilder kbuilder = kieServices.newKieBuilder(kieFileSystem);
        // Build the Kie Bases
        kbuilder.buildAll();
        // Check for errors
        if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
            log.error("Error found : " + kbuilder.getResults().getMessages());
            return false;
        }
        // Get the Release ID (mvn style: groupId, artifactId,version)
        ReleaseId relId = kbuilder.getKieModule().getReleaseId();
        Collection<InternalKnowledgePackage> knowledgeBaseCollection;
        if (kieContainer == null) {
            kieContainer = kieServices.newKieContainer(relId);
            knowledgeBaseCollection = ((InternalKnowledgeBase) kieContainer.getKieBase()).getPackagesMap().values();
        } else {
            KieContainer tempKieContainer = kieServices.newKieContainer(relId);
            knowledgeBaseCollection = ((InternalKnowledgeBase) tempKieContainer.getKieBase()).getPackagesMap().values();
            ((InternalKnowledgeBase) kieContainer.getKieBase()).addPackages(knowledgeBaseCollection);
        }
        for (KiePackage kp : knowledgeBaseCollection) {
            for (Rule rule : kp.getRules()) {
                regulationRuleMap.put(rule.getName(), new RegulationRule(rule.getName()));
            }
        }
        return true;
    }

    public boolean removeRule(String ruleName) {
        try {
            kieContainer.getKieBase().removeRule("defaultpkg", ruleName);
            regulationRuleMap.remove(ruleName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void addRegulationRule(RegulationRule RegulationRule) {
        this.regulationRuleMap.put(RegulationRule.getId(), RegulationRule);
    }

    public RegulationRule getRegulationRule(String id) {
        return regulationRuleMap.get(id);
    }

    public boolean containsRegulationRule(String id) {
        return regulationRuleMap.containsKey(id);
    }

    public RegulationRule removeRegulationRule(String id) {
        return regulationRuleMap.remove(id);
    }
}
