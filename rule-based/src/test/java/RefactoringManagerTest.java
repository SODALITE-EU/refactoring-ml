import nl.jads.sodalite.dto.AADMModel;
import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;
import org.json.simple.parser.ParseException;
import tosca.mapper.dto.Node;

import java.io.FileWriter;
import java.io.IOException;

public class RefactoringManagerTest {

    public static void main(String[] args) {
       
    }
    private static Node loadNode(RefactoringManager manager){
        try {
            Node node = manager.findMatchingNodeFromRM("( ?name = \"snow-vm_new_2\" )");
            System.out.println(node.getOfType() + node.getName());
            return node;
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
            aadmModel.updateRequirement("snow-skyline-extractor", "host", "snow-docker-host");
            aadmModel.updateRequirement("snow-skyline-alignment", "host", "snow-docker-host");
            aadmModel.updateRequirement("snow-configuration-demo", "remote_server", "snow-vm");
            aadmModel.removeRequirementWithValue("snow-docker-registry", "dependency",
                    "snow-docker-registry-certificate-2");
            manager.saveDeploymentModelInKB();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void createNewAADM2VMs(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getOriginalDeploymentInfo();
            Node snowvm2node = manager.findMatchingNodeFromRM("( ?name = \"snow-vm_new_2\" )");
//            Node
            manager.loadRefactoredDeployment();
            AADMModel aadmModel = manager.getAadm();
            aadmModel.addNode(snowvm2node);
            aadmModel.removeNode("snow-docker-host-2");
            aadmModel.removeNode("snow-docker-registry-certificate-2");
            aadmModel.updateRequirement("snow-skyline-extractor", "host", "snow-docker-host");
            aadmModel.updateRequirement("snow-skyline-alignment", "host", "snow-docker-host");
            aadmModel.updateRequirement("snow-configuration-demo", "remote_server", "snow-vm");
            aadmModel.removeRequirementWithValue("snow-docker-registry", "dependency",
                    "snow-docker-registry-certificate-2");
            manager.saveDeploymentModelInKB();
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
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnbrefac");
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
            DeploymentInfo deploymentInfo = manager.getOriginalDeploymentInfo();
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/opgr7qto1uv6n96i4eqkfv4k8o/AADM_3kmq0iknmsd0s89rnba0hhr88erefac");
            deploymentInfo.setDeployment_id("511d8a5d-1198-49b6-8d7f-4741df893dad");
            manager.loadRefactoredDeployment();

            manager.getAadm().updateProperty("snow-daily-median-aggregator", "restart_policy", "on-failure");
            manager.getOriginalDeploymentInfo().updateInput("flavor-name", "m1.medium");

            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();

            manager.update(deploymentInfo.getDeployment_id(), deploymentInfo.getBlueprint_id(), deploymentInfo.getUpdatedInput());
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
