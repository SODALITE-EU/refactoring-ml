package nl.jads.sodalite.utils;


import nl.jads.sodalite.dto.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DeploymentModelBuilder {

    public static DeploymentModelJSON fromJsonText(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);
        return fromJsonObject(json);
    }

    public static DeploymentModelJSON fromJsonFile(String path) throws IOException, ParseException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        Object obj = new JSONParser().parse(new InputStreamReader(in));
        in.close();
        // typecasting obj to JSONObject
        return fromJsonObject((JSONObject) obj);

    }

    private static DeploymentModelJSON fromJsonObject(JSONObject jo) {
        DeploymentModelJSON dm = new DeploymentModelJSON();
        Set<Map.Entry> elements = jo.entrySet();
        for (Iterator it = elements.iterator(); it.hasNext(); ) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();
            JSONObject jsonObject = (JSONObject) pair.getValue();
            String type = (String) jsonObject.get(DTOConstraints.TYPE);
            if (type != null && type.equals(DTOConstraints.ABSTRACT_APPLICATION_DEPLOYMENT_MODEL)) {
                buildDMRoot(jsonObject, dm);
                continue;
            }
            Object isNodeTemplate = jsonObject.get(DTOConstraints.IS_NODE_TEMPLATE);
            if (isNodeTemplate != null) {
                buildNode(jsonObject, dm, key);
            }
            Object inputs = jsonObject.get(DTOConstraints.INPUTS);
            if (inputs != null) {
                buildDMInputs(jsonObject, dm, key);
            }
        }
        return dm;
    }

    private static void buildDMRoot(JSONObject jsonObject, DeploymentModelJSON dm) {
        dm.setVersion((String) jsonObject.get(DTOConstraints.VERSION));
        dm.setCreatedAt((String) jsonObject.get(DTOConstraints.CREATED_AT));
        dm.setCreatedBy((String) jsonObject.get(DTOConstraints.CREATED_BY));
        dm.setId((String) jsonObject.get(DTOConstraints.ID));
        dm.setNamespace((String) jsonObject.get(DTOConstraints.NAME_SPACE));
        dm.setParticipants((JSONArray) jsonObject.get(DTOConstraints.PARTICIPANTS));
    }

    private static void buildDMInputs(JSONObject jsonObject, DeploymentModelJSON dm, String key) {
        dm.setInputKey(key);
        JSONArray inputsArray = (JSONArray) jsonObject.get(DTOConstraints.INPUTS);
        if (inputsArray != null) {
            for (Iterator it = inputsArray.iterator(); it.hasNext(); ) {
                JSONObject req = (JSONObject) it.next();
                String pName = (String) req.keySet().toArray()[0];
                JSONObject jObj = (JSONObject) req.get(pName);
                Input input = new Input();
                input.setName(pName);
                input.setSpecification((JSONObject) jObj.get(DTOConstraints.SPEC));
                dm.addInput(input);
            }
        }
    }

    private static void buildNode(JSONObject jsonObject, DeploymentModelJSON dm, String key) {
        Node node = new Node();
        node.setName(key);
        node.setType((String) jsonObject.get(DTOConstraints.TYPE));
        node.setNodeTemplate((Boolean) jsonObject.get(DTOConstraints.IS_NODE_TEMPLATE));
        JSONArray jsonArray = (JSONArray) jsonObject.get(DTOConstraints.PROPERTIES);
        if (jsonArray != null) {
            for (Iterator it = jsonArray.iterator(); it.hasNext(); ) {
                JSONObject prop = (JSONObject) it.next();
                String pName = (String) prop.keySet().toArray()[0];
                JSONObject jObj = (JSONObject) prop.get(pName);
                Property property = new Property();
                property.setName(pName);
                property.setSpecification((JSONObject) jObj.get(DTOConstraints.SPEC));
                property.setLabel((String) jObj.get(DTOConstraints.LABEL));
                property.setDescription((String) jObj.get(DTOConstraints.DESC));
                Object values = jObj.get(DTOConstraints.VALUE);
                if (values != null) {
                    if (values instanceof JSONArray) {
                        JSONArray pAry = (JSONArray) values;
                        for (Iterator it2 = pAry.iterator(); it2.hasNext(); ) {
                            property.addValue((String) it2.next());
                        }
                    } else {
                        property.addValue((String) values);
                    }
                }
                node.addProperty(property);
            }
        }
        JSONArray jsonArrayReqs = (JSONArray) jsonObject.get(DTOConstraints.REQS);
        if (jsonArrayReqs != null) {
            for (Iterator it = jsonArrayReqs.iterator(); it.hasNext(); ) {
                JSONObject req = (JSONObject) it.next();
                String pName = (String) req.keySet().toArray()[0];
                JSONObject jObj = (JSONObject) req.get(pName);
                Requirement requirement = new Requirement();
                requirement.setName(pName);
                requirement.setSpecification((JSONObject) jObj.get(DTOConstraints.SPEC));
                node.addRequirement(requirement);
            }
        }
        JSONArray jsonArrayAttr = (JSONArray) jsonObject.get(DTOConstraints.ATTRIBUTES);
        if (jsonArrayAttr != null) {
            for (Iterator it = jsonArrayAttr.iterator(); it.hasNext(); ) {
                JSONObject attri = (JSONObject) it.next();
                String pName = (String) attri.keySet().toArray()[0];
                JSONObject jObj = (JSONObject) attri.get(pName);
                Attribute attribute = new Attribute();
                attribute.setName(pName);
                attribute.setSpecification((JSONObject) jObj.get(DTOConstraints.SPEC));
                attribute.setDescription((String) jObj.get(DTOConstraints.DESC));
                node.addAttribute(attribute);
            }
        }

        JSONArray jsonArrayCaps = (JSONArray) jsonObject.get(DTOConstraints.CAPABILITIES);
        if (jsonArrayCaps != null) {
            for (Iterator it = jsonArrayCaps.iterator(); it.hasNext(); ) {
                JSONObject req = (JSONObject) it.next();
                String pName = (String) req.keySet().toArray()[0];
                JSONObject jObj = (JSONObject) req.get(pName);
                Capability capability = new Capability();
                capability.setName(pName);
                capability.setSpecification((JSONObject) jObj.get(DTOConstraints.SPEC));
                node.addCapability(capability);
            }
        }
        JSONArray jsonArrayInterfaces = (JSONArray) jsonObject.get(DTOConstraints.INTERFACES);
        if (jsonArrayInterfaces != null) {
            for (Iterator it = jsonArrayInterfaces.iterator(); it.hasNext(); ) {
                JSONObject req = (JSONObject) it.next();
                String pName = (String) req.keySet().toArray()[0];
                JSONObject jObj = (JSONObject) req.get(pName);
                Interface anInterface = new Interface();
                anInterface.setName(pName);
                anInterface.setSpecification((JSONObject) jObj.get(DTOConstraints.SPEC));
                node.addInterface(anInterface);
            }
        }
        dm.addNode(node);
    }
}
