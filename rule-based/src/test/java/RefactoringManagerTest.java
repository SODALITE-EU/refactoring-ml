import nl.jads.sodalite.rules.RefactoringManager;

public class RefactoringManagerTest {

    public static void main(String[] args) {
        RefactoringManager manager = new RefactoringManager();
        manager.setReasonerUri("http://.....:8080/reasoner-api/v0.6/");
        manager.setApikey("test");
        try {
            manager.loadDeployment(
                    "https://www.sodalite.eu/ontologies/workspace/1/vbeit9auui3d3j0tdekbljfndl/AADM_92aj0uo7t6l6u8mv5tmh99pjnb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
