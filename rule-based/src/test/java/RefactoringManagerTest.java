import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;

public class RefactoringManagerTest {

    public static void main(String[] args) {
        RefactoringManager manager = new RefactoringManager();
        manager.setReasonerUri("http://....:8080/reasoner-api/v0.6/");
        manager.setApikey("test");
        manager.setClientId("sodalite-ide");
        manager.setClientSecret("1a1083bc-c183-416a-9192-26076f605cc3");
        manager.setAuthUri("http://....:8080/");
        manager.setXopera("http://....:5000/");
        manager.setIacBuilderUri("http://....:8081/");
        manager.setUsername("indika");
        manager.setPassword("....");
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setInput("...");
        manager.setCurrentDeploymentInfo(deploymentInfo);
        try {
//            AADMModel aadmModel = AADMModelBuilder.fromJsonFile("aadm_simple.json");
//            if (aadmModel != null) {
//                aadmModel.updateNodeTypes();
//                manager.setAadm(aadmModel);
//                BufferedWriter writer = new BufferedWriter(new FileWriter("aadm_simple.ttl"));
//                writer.write(aadmModel.getExchangeAADM());
//                writer.close();
//                manager.saveDeploymentModelInKB();
//            manager.buildIaC(
//                    "https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnbrefac", "snowrefac");
//            }
            manager.deploy("ffcc5197-b791-41a1-adbe-ee270cedd38c", deploymentInfo.getInput());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
