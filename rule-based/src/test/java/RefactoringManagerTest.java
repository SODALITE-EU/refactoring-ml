import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;

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
        manager.setPassword("...");
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setInput("... ");
        manager.setCurrentDeploymentInfo(deploymentInfo);
        deploy(manager);
    }

    private static void deploy(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getCurrentDeploymentInfo();
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnb");
            manager.loadCurrentDeployment();
            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();
            manager.deployCurrentDeployment();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_token());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void update(RefactoringManager manager) {
        try {
            DeploymentInfo deploymentInfo = manager.getCurrentDeploymentInfo();
            deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnbrefac");
            deploymentInfo.setDeployment_id("...");
            manager.loadCurrentDeployment();

            manager.getAadm().updateProperty("snow-daily-median-aggregator", "restart_policy", "on-failure");
            manager.getCurrentDeploymentInfo().updateInput("flavor-name", "m1.medium");

            manager.saveDeploymentModelInKB();
            manager.buildIaCForCurrentDeployment();

            manager.update(deploymentInfo.getDeployment_id(), deploymentInfo.getBlueprint_token(), deploymentInfo.getUpdatedInput());
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_token());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
