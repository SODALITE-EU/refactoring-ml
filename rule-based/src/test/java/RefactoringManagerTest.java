import nl.jads.sodalite.dto.AADMModel;
import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;
import org.json.simple.parser.ParseException;
import tosca.mapper.dto.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class RefactoringManagerTest {

    public static void main(String[] args) {

    }

    private static List<Node> loadNode(RefactoringManager manager) {
        try {
//            Node node = manager.findMatchingNodeFromRM("( ?name = \"snow-vm_new_2\" )");
            List<Node> nodes = manager.getNodeMatchingReqFromRM("snow/snow-vm-2");
            for (Node node : nodes) {
                System.out.println(node.getOfType() + node.getName());
                return nodes;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void createNewAADM(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getOriginalDeploymentInfo();
            manager.loadRefactoredDeployment();
            AADMModel aadmModel = manager.getAadm();
            aadmModel.removeNode("snow-vm-2");
            aadmModel.removeNode("snow-docker-host-2");
            aadmModel.removeNode("snow-docker-registry-certificate-2");
            aadmModel.updateArrayProperty("snow-skyline-extractor", "ports", "8082:8080");
            aadmModel.updateArrayProperty("snow-skyline-alignment", "ports", "8081:8080,82:8080");
            aadmModel.updateRequirement("snow-skyline-extractor", "host", "snow-docker-host");
            aadmModel.updateRequirement("snow-skyline-alignment", "host", "snow-docker-host");
            aadmModel.updateRequirement("snow-configuration-demo", "remote_server", "snow-vm");
            aadmModel.removeRequirementWithValue("snow-docker-registry", "dependency",
                    "snow-docker-registry-certificate-2");
            manager.saveDeploymentModelInKB();
//            manager.saveAndUpdate();
            deploymentInfo = manager.getRefactoredDeploymentInfo();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createNewAADM2VMs(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getRefactoredDeploymentInfo();
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnbrefac");
            manager.loadRefactoredDeployment();
            Node snowvm2node = manager.findMatchingNodeFromRM("( ?name = \"snow-vm_new_2\" )");
            manager.loadRefactoredDeployment();
            AADMModel aadmModel = manager.getAadm();
            aadmModel.addNode(snowvm2node);
            List<Node> nodes = manager.getNodeMatchingReqFromRM("snow/snow-vm-2");
            for (Node node : nodes) {
                aadmModel.addNode(node);
            }
            aadmModel.updateArrayProperty("snow-skyline-extractor", "ports", "8080:8080");
            aadmModel.updateArrayProperty("snow-skyline-alignment", "ports", "8081:8080,80:8080");
            aadmModel.updateRequirement("snow-skyline-extractor", "host", "snow-docker-host-2");
            aadmModel.updateRequirement("snow-skyline-alignment", "host", "snow-docker-host-2");
            aadmModel.addRequirement("snow-docker-registry", manager.createRequirement("dependency",
                    "node: snow-docker-registry-certificate-2"));
            aadmModel.updateRequirement("snow-configuration-demo", "remote_server", "snow-vm-2");
            aadmModel.updateNodeTypes();
//            saveAsFile("snow3.ttl", aadmModel.getExchangeAADM());
//            manager.saveDeploymentModelInKB();
//            manager.buildIaCForCurrentDeployment();
//            manager.saveAndUpdate();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deploy(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getRefactoredDeploymentInfo();
//            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnbrefac");
//            saveAsFile("snow.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
            manager.loadRefactoredDeployment();
//              manager.getAadm().getExchangeAADM();
//            manager.saveDeploymentModelInKB();
//            saveAsFile("snowrefac.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
            manager.buildIaCForCurrentDeployment();
            manager.deployCurrentDeployment();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void update(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getRefactoredDeploymentInfo();
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnbrefac");
            deploymentInfo.setDeployment_id("6174db74-6a83-43d1-9073-599b8abded54");
            manager.loadRefactoredDeployment();
            deploymentInfo.updateInput("flavor-name", "m1.medium");
            manager.saveAndUpdate();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void delete(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getOriginalDeploymentInfo();
            deploymentInfo.setDeployment_id("3109dc19-aa67-44e3-a79d-0b7b5a144b8d");
            manager.unDeployCurrentDeployment();
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

}
