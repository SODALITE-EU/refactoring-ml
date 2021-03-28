import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;

import java.io.FileWriter;
import java.io.IOException;

public class RefactoringManagerTest {

    public static void main(String[] args) {
        RefactoringManager manager = new RefactoringManager();
        manager.setReasonerUri("...");
        manager.setApikey("test");
        manager.setClientId("sodalite-ide");
        manager.setClientSecret("1...");
        manager.setAuthUri("...:8080/");
        manager.setXopera("...:5000/");
        manager.setIacBuilderUri("...:8081/");
        manager.setUsername("indika");
        manager.setPassword("qwerty123");
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setInput("... ");
        manager.setCurrentDeploymentInfo(deploymentInfo);
//        deploy(manager);
    }

    private static void deploy(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getCurrentDeploymentInfo();
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/opgr7qto1uv6n96i4eqkfv4k8o/AADM_3kmq0iknmsd0s89rnba0hhr88e");
//            saveAsFile("snow.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
            manager.loadCurrentDeployment();
//              manager.getAadm().getExchangeAADM();
//            manager.saveDeploymentModelInKB();
//            saveAsFile("snowrefac.json", manager.getDeploymentSimple(deploymentInfo.getAadm_id()));
//            manager.buildIaCForCurrentDeployment();
//            manager.deployCurrentDeployment();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_id());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void update(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getCurrentDeploymentInfo();
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/opgr7qto1uv6n96i4eqkfv4k8o/AADM_3kmq0iknmsd0s89rnba0hhr88erefac");
            deploymentInfo.setDeployment_id("511d8a5d-1198-49b6-8d7f-4741df893dad");
            manager.loadCurrentDeployment();

            manager.getAadm().updateProperty("snow-daily-median-aggregator", "restart_policy", "on-failure");
            manager.getCurrentDeploymentInfo().updateInput("flavor-name", "m1.medium");

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
            DeploymentInfo deploymentInfo = manager.getCurrentDeploymentInfo();
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
