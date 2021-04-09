import nl.jads.sodalite.dto.AADMModel;
import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;
import org.json.simple.parser.ParseException;
import tosca.mapper.dto.Node;
import tosca.mapper.dto.Property;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RefactoringManagerVehicleIoTTest {

    public static void main(String[] args) {
       
    }

    private static void loadNode(RefactoringManager manager) {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/7ke93c8pgi7piknhgaat0q1n00/AADM_9gfoiqlpkkuh4thirvufqr2kl2");
        deploymentInfo.setInput("");
        manager.setOriginalDeploymentInfo(deploymentInfo);
        try {
            Node node = manager.findMatchingNodeFromRM(
                    "( ?edgetpus = "+ 1 +" ) && ( ?cpus = "+ 1+" )" + " && ( ?ready_status = "+ true+" )");
            System.out.println(node.getOfType());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private static void deploy(RefactoringManager manager) {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/7ke93c8pgi7piknhgaat0q1n00/AADM_9gfoiqlpkkuh4thirvufqr2kl2");
        deploymentInfo.setInput("");
//        saveAsFile("vehicle.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
//        deploymentInfo.setBlueprint_token("19ff5810-93f0-47e3-b117-86c0a1482cea");
        manager.setOriginalDeploymentInfo(deploymentInfo);
        try {
            manager.loadRefactoredDeployment();
//            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();
            manager.deployCurrentDeployment();
//            manager.getAadm().getExchangeAADM();
//            saveAsFile("vehicleref.ttl", manager.getAadm().getExchangeAADM());
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deployEdgeTPU(RefactoringManager manager) {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/7ke93c8pgi7piknhgaat0q1n00/AADM_9gfoiqlpkkuh4thirvufqr2kl2");
        deploymentInfo.setInput("");
        manager.setOriginalDeploymentInfo(deploymentInfo);
        try {
            manager.loadRefactoredDeployment();

            AADMModel aadmModel = manager.getAadm();
            aadmModel.removeNode("node-filesrv");
            aadmModel.removeNode("node-xavier-nx-gpu-cpu");

            String celsius = "node-sgx-celsius-w550power";
            Node node = new Node(celsius);
            node.setOfType("kube/sodalite.nodes.Kubernetes.Node");
            Set<Property> properties = new HashSet<>();
            properties.add(manager.createProperty("name", "sgx-celsius-w550power"));
            properties.add(manager.createProperty("ready_status", String.valueOf(true)));
            properties.add(manager.createProperty("edgetpus", String.valueOf(1)));
            properties.add(manager.createProperty("cpus", String.valueOf(1)));
            properties.add(manager.createProperty("amd64_cpus", String.valueOf(1)));
            node.setProperties(properties);
            aadmModel.addNode(node);
            aadmModel.updateRequirement("mysql-deployment-via-helm", "kube_node", celsius);

            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();
            manager.deployCurrentDeployment();
//            saveAsFile("vehicleref.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
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
        deploymentInfo.setBlueprint_id("3bee2a0d-b367-409b-a967-037ced7f4b56");
        manager.setOriginalDeploymentInfo(deploymentInfo);
        try {
            manager.loadRefactoredDeployment();

            AADMModel aadmModel = manager.getAadm();
            aadmModel.removeNode("node-filesrv");

            String xavier = "node-xavier-nx-gpu-cpu";
            Node node = new Node(xavier);
            node.setOfType("kube/sodalite.nodes.Kubernetes.Node");
            Set<Property> properties = new HashSet<>();
            properties.add(manager.createProperty("name", "xavier-nx"));
            properties.add(manager.createProperty("ready_status", String.valueOf(true)));
            properties.add(manager.createProperty("gpus", String.valueOf(1)));
            properties.add(manager.createProperty("cpus", String.valueOf(1)));
            properties.add(manager.createProperty("arm64_cpus", String.valueOf(1)));
            node.setProperties(properties);
            aadmModel.addNode(node);
            aadmModel.updateRequirement("mysql-deployment-via-helm", "kube_node", xavier);
            aadmModel.getExchangeAADM();
            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();
            manager.update(deploymentInfo.getDeployment_id(),
                    deploymentInfo.getBlueprint_id(), deploymentInfo.getInputs());
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void delete(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = new DeploymentInfo();
            deploymentInfo.setDeployment_id("612efea0-c666-42de-9803-5adce8d59eac");
            manager.setOriginalDeploymentInfo(deploymentInfo);
            manager.unDeployCurrentDeployment();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
