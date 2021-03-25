import nl.jads.sodalite.dto.AADMModel;
import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;
import tosca.mapper.dto.Node;
import tosca.mapper.dto.Property;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RefactoringManagerVehicleIoTTest {

    public static void main(String[] args) {
        RefactoringManager manager = new RefactoringManager();
        manager.setReasonerUri("...");
        manager.setApikey("test");
        manager.setClientId("...");
        manager.setClientSecret("...");
        manager.setAuthUri("....");
        manager.setXopera("...");
        manager.setIacBuilderUri("..");
        manager.setUsername("...");
        manager.setPassword("...");
//        deploy(manager);
//        update(manager);
    }

    private static void deploy(RefactoringManager manager) {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/7ke93c8pgi7piknhgaat0q1n00/AADM_9gfoiqlpkkuh4thirvufqr2kl2");
        deploymentInfo.setInput("");

//        saveAsFile("vehicle.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
//        deploymentInfo.setBlueprint_token("19ff5810-93f0-47e3-b117-86c0a1482cea");
        manager.setCurrentDeploymentInfo(deploymentInfo);
        try {
            manager.loadCurrentDeployment();
            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();
            manager.deployCurrentDeployment();
//            saveAsFile("vehicleref.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_token());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveAsFile(String filename, String value) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(value);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void update(RefactoringManager manager) {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/7ke93c8pgi7piknhgaat0q1n00/AADM_9gfoiqlpkkuh4thirvufqr2kl2");
        deploymentInfo.setInput("");
        deploymentInfo.setDeployment_id("10e28566-1bb1-42c6-924b-95b9afeae745");
        deploymentInfo.setBlueprint_token("3bee2a0d-b367-409b-a967-037ced7f4b56");
        manager.setCurrentDeploymentInfo(deploymentInfo);
        try {
            manager.loadCurrentDeployment();

            AADMModel aadmModel = manager.getAadm();
            aadmModel.removeNode("node-filesrv");

            String xavier = "node-xavier-nx-gpu-cpu";
            Node node = new Node(xavier);
            node.setOfType("kube/sodalite.nodes.Kubernetes.Node");
            Set<Property> properties = new HashSet<>();
            properties.add(createProperty("name", "xavier-nx"));
            properties.add(createProperty("ready_status", true));
            properties.add(createProperty("gpus", 1));
            properties.add(createProperty("gpus", 1));
            properties.add(createProperty("cpus", 1));
            properties.add(createProperty("arm64_cpus", 1));
            node.setProperties(properties);
            aadmModel.addNode(node);
            aadmModel.updateRequirement("mysql-deployment-via-helm", "kube_node", xavier);
            aadmModel.getExchangeAADM();
            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();
            manager.update(deploymentInfo.getDeployment_id(),
                    deploymentInfo.getBlueprint_token(), deploymentInfo.getInputs());
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_token());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void delete(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getCurrentDeploymentInfo();
            deploymentInfo.setDeployment_id("10e28566-1bb1-42c6-924b-95b9afeae745");
            manager.unDeployCurrentDeployment();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_token());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Property createProperty(String name, Object value) {
        Property property = new Property(name);
        property.setValue(String.valueOf(value));
        return property;
    }
}
