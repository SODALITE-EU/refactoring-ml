import nl.jads.sodalite.dto.AADMModel;
import nl.jads.sodalite.dto.DeploymentInfo;
import nl.jads.sodalite.rules.RefactoringManager;
import tosca.mapper.dto.Node;
import tosca.mapper.dto.Property;

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
        update(manager);
    }

    private static void deploy(RefactoringManager manager) {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/7ke93c8pgi7piknhgaat0q1n00/AADM_9gfoiqlpkkuh4thirvufqr2kl2refac");
        deploymentInfo.setInput("");
        deploymentInfo.setBlueprint_token("19ff5810-93f0-47e3-b117-86c0a1482cea");
        manager.setCurrentDeploymentInfo(deploymentInfo);
        try {
//            manager.loadCurrentDeployment();
//            manager.saveDeploymentModelInKB();
//            manager.buildIaCForCurrentDeployment();
            manager.deployCurrentDeployment();
            System.out.println(deploymentInfo.getAadm_id());
            System.out.println(deploymentInfo.getBlueprint_token());
            System.out.println(deploymentInfo.getDeployment_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void update(RefactoringManager manager) {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setAadm_id("https://www.sodalite.eu/ontologies/workspace/1/7ke93c8pgi7piknhgaat0q1n00/AADM_9gfoiqlpkkuh4thirvufqr2kl2refac");
        deploymentInfo.setInput("");
        deploymentInfo.setDeployment_id("7e99e3fc-a1b9-4e95-9a4f-2678f46fb1e8");
        deploymentInfo.setBlueprint_token("19ff5810-93f0-47e3-b117-86c0a1482cea");
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

    private static Property createProperty(String name, Object value) {
        Property property = new Property(name);
        property.setValue(String.valueOf(value));
        return property;
    }
}
