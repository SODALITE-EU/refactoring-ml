package nl.jads.sodalite.utils;

import nl.jads.sodalite.dto.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.List;

public class DeploymentModelSerializer {
    public static JSONObject toJson(DeploymentModelJSON dm) {
        JSONObject dmJson = new JSONObject();
        JSONObject aadmMeta = new JSONObject();
        dmJson.put(dm.getId(), aadmMeta);
        aadmMeta.put(DTOConstraints.VERSION, dm.getVersion());
        aadmMeta.put(DTOConstraints.CREATED_AT, dm.getCreatedAt());
        aadmMeta.put(DTOConstraints.CREATED_BY, dm.getCreatedBy());
        aadmMeta.put(DTOConstraints.NAME_SPACE, dm.getNamespace());
        aadmMeta.put(DTOConstraints.PARTICIPANTS, dm.getParticipants());
        aadmMeta.put(DTOConstraints.ID, dm.getId());
        aadmMeta.put(DTOConstraints.TYPE, DTOConstraints.ABSTRACT_APPLICATION_DEPLOYMENT_MODEL);

        for (Node node : dm.getNodes()) {
            JSONObject nodeObject = new JSONObject();
            dmJson.put(node.getName(), nodeObject);
            nodeObject.put(DTOConstraints.IS_NODE_TEMPLATE, node.isNodeTemplate());
            nodeObject.put(DTOConstraints.TYPE, node.getType());
            //Serialize properties
            if (!node.getProperties().isEmpty()) {
                JSONArray propertiesJson = new JSONArray();
                nodeObject.put(DTOConstraints.PROPERTIES, propertiesJson);
                for (Property property : node.getProperties()) {
                    JSONObject propJsonRoot = new JSONObject();
                    JSONObject propJson = new JSONObject();
                    propJsonRoot.put(property.getName(), propJson);
                    propertiesJson.add(propJsonRoot);
                    JSONObject spec = property.getSpecification();
                    if (spec != null) {
                        propJson.put(DTOConstraints.SPEC, spec);
                    }

                    String desc = property.getDescription();
                    if (desc != null && !desc.isEmpty()) {
                        propJson.put(DTOConstraints.DESC, desc);
                    }
                    List<String> values = property.getValues();
                    if (!values.isEmpty()) {
                        if (values.size() == 1) {
                            propJson.put(DTOConstraints.VALUE, values.get(0));
                        } else {
                            JSONArray valArray = new JSONArray();
                            for (String val : values) {
                                valArray.add(val);
                            }
                            propJson.put(DTOConstraints.VALUE, valArray);
                        }
                    }
                    String label = property.getLabel();
                    if (label != null && !label.isEmpty()) {
                        propJson.put(DTOConstraints.LABEL, label);
                    }
                }
            }
            //Serialize attributes
            if (!node.getAttributes().isEmpty()) {
                serializeNamedElement(nodeObject, node.getAttributes(), DTOConstraints.ATTRIBUTES);
            }
            //Serialize requirements
            if (!node.getRequirements().isEmpty()) {
                serializeNamedElement(nodeObject, node.getRequirements(), DTOConstraints.REQS);
            }
            //Serialize capabilities
            if (!node.getCapabilities().isEmpty()) {
                serializeNamedElement(nodeObject, node.getCapabilities(), DTOConstraints.CAPABILITIES);
            }
            //Serialize interfaces
            if (!node.getInterfaces().isEmpty()) {
                serializeNamedElement(nodeObject, node.getInterfaces(), DTOConstraints.INTERFACES);
            }
        }
        //Serialize inputs
        if (!dm.getInputs().isEmpty()) {
            JSONObject inputsJson = new JSONObject();
            dmJson.put(dm.getInputKey(), inputsJson);
            serializeNamedElement(inputsJson, dm.getInputs(), DTOConstraints.INPUTS);
        }
        return dmJson;
    }

    private static void serializeNamedElement(JSONObject nodeObject, Collection<NamedElement> elements, String type) {
        JSONArray jsonArray = new JSONArray();
        nodeObject.put(type, jsonArray);
        for (NamedElement element : elements) {
            JSONObject jsonRoot = new JSONObject();
            JSONObject json = new JSONObject();
            jsonRoot.put(element.getName(), json);
            JSONObject spec = element.getSpecification();
            if (spec != null) {
                json.put(DTOConstraints.SPEC, spec);
            }
            String desc = element.getDescription();
            if (desc != null && !desc.isEmpty()) {
                json.put(DTOConstraints.DESC, desc);
            }
            jsonArray.add(json);
        }
    }
}
