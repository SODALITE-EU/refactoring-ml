package nl.jads.sodalite.utils;


import nl.jads.sodalite.dto.AADMModel;
import nl.jads.sodalite.dto.DTOConstraints;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tosca.mapper.dto.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class AADMModelBuilder {

    public static AADMModel fromJsonText(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);
        return fromJsonObject(json);
    }

    public static AADMModel fromJsonFile(String path) throws IOException, ParseException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        Object obj = new JSONParser().parse(new InputStreamReader(in));
        in.close();
        // typecasting obj to JSONObject
        return fromJsonObject((JSONObject) obj);
    }

    private static AADMModel fromJsonObject(JSONObject jo) {
        AADMModel dm = new AADMModel();
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
                dm.addProperties(createProperties((JSONArray) inputs));
            }
        }
        return dm;
    }

    private static void buildDMRoot(JSONObject jsonObject, AADMModel dm) {
        dm.setVersion((String) jsonObject.get(DTOConstraints.VERSION));
        dm.setId((String) jsonObject.get(DTOConstraints.ID));
        String nameSpace = (String) jsonObject.get(DTOConstraints.NAME_SPACE);
//        if (!nameSpace.endsWith("refac")) {
//            nameSpace = nameSpace + "refac";
//        }
        dm.setNamespace(nameSpace);
    }


    private static void buildNode(JSONObject jsonObject, AADMModel dm, String key) {
        dm.addNode(toNode(jsonObject,key));
    }

    public static Node toNode(JSONObject jsonObject, String key){
        Node node = new Node(key.substring(key.lastIndexOf("/") + 1));
        String type = (String) jsonObject.get(DTOConstraints.TYPE);
        String context = type.substring(0, type.lastIndexOf("/"));
        context = context.substring(context.lastIndexOf("/") + 1);
        node.setOfType(context + "/" + type.substring(type.lastIndexOf("/") + 1));
        JSONArray jsonArray = (JSONArray) jsonObject.get(DTOConstraints.PROPERTIES);
        if (jsonArray != null) {
            node.setProperties(createProperties(jsonArray));
        }
        JSONArray jsonArrayReqs = (JSONArray) jsonObject.get(DTOConstraints.REQS);
        if (jsonArrayReqs != null) {
            node.setRequirements(createRequirements(jsonArrayReqs, type));
        }
        JSONArray jsonArrayAttr = (JSONArray) jsonObject.get(DTOConstraints.ATTRIBUTES);
        if (jsonArrayAttr != null) {
            node.setAttributes(createAttributes(jsonArrayAttr));
        }
//
//        JSONArray jsonArrayCaps = (JSONArray) jsonObject.get(DTOConstraints.CAPABILITIES);
//        if (jsonArrayCaps != null) {
//            Set<Capability> capabilities = new HashSet<>();
//            for (Iterator it = jsonArrayCaps.iterator(); it.hasNext(); ) {
//                JSONObject req = (JSONObject) it.next();
//                String pName = (String) req.keySet().toArray()[0];
//                JSONObject jObj = (JSONObject) req.get(pName);
//                Capability capability = new Capability(pName.substring(pName.lastIndexOf("/" )+1));
//                capabilities.add(capability);
//            }
//            node.setCapabilities(capabilities);
//        }
////        JSONArray jsonArrayInterfaces = (JSONArray) jsonObject.get(DTOConstraints.INTERFACES);
////        if (jsonArrayInterfaces != null) {
////            for (Iterator it = jsonArrayInterfaces.iterator(); it.hasNext(); ) {
////                JSONObject req = (JSONObject) it.next();
////                String pName = (String) req.keySet().toArray()[0];
////                JSONObject jObj = (JSONObject) req.get(pName);
////                Interface anInterface = new Interface();
////                anInterface.setName(pName);
////                anInterface.setSpecification((JSONObject) jObj.get(DTOConstraints.SPEC));
////                node.addInterface(anInterface);
////            }
////        }
        return node;
    }

    private static Parameter createParameter(JSONObject o, String key) {
        Parameter parameter = new Parameter(key);
        Object value = o.get(key);
        if (value == null && !o.keySet().isEmpty()) {
            Set<Parameter> parameters = new HashSet<>();
            for (Object p : o.keySet()) {
                Parameter para = new Parameter((String) p);
                para.setValue(String.valueOf(o.get(p)));
                parameters.add(para);
            }
            parameter.setParameters(parameters);
        } else {
            parameter.setValue(String.valueOf(value));
        }
        return parameter;
    }

    private static Set<Requirement> createRequirements(JSONArray jsonArrayReqs, String type) {
        Set<Requirement> requirements = new HashSet<>();
        for (Iterator it = jsonArrayReqs.iterator(); it.hasNext(); ) {
            JSONObject req = (JSONObject) it.next();
            String pName = (String) req.keySet().toArray()[0];
            JSONObject jObj = (JSONObject) req.get(pName);

            JSONObject spec = (JSONObject) jObj.get(DTOConstraints.SPEC);
            if (spec != null) {
                Object nodeR = spec.get(DTOConstraints.NODE);
                if (nodeR != null) {
                    JSONObject nodeO = (JSONObject) nodeR;
                    Requirement requirement = new Requirement(pName.substring(pName.lastIndexOf("/") + 1));
                    Set<Parameter> children = new HashSet<>();
                    children.add(processReqNode((String) nodeO.keySet().toArray()[0], type, pName));
                    requirement.setParameters(children);
                    requirements.add(requirement);
                }
            } else {
                Object value = jObj.get(DTOConstraints.VALUE);
                if (value != null) {
                    JSONObject nodeO = (JSONObject) value;
                    for (Object o3 : nodeO.keySet()) {
                        if (!DTOConstraints.LABEL.equals(o3)) {
                            Requirement requirement = new Requirement(pName.substring(pName.lastIndexOf("/") + 1));
                            Set<Parameter> children = new HashSet<>();
                            children.add((processReqNode((String) o3, type, pName)));
                            requirement.setParameters(children);
                            requirements.add(requirement);
                        }
                    }
                }
            }
        }
        return requirements;
    }

    private static Parameter processReqNode(String nodeKey, String type, String pName) {
        Parameter parameter = new Parameter(DTOConstraints.NODE);
        System.out.println(nodeKey);
        String contextReq = nodeKey.substring(0, type.lastIndexOf("/"));
        contextReq = contextReq.substring(contextReq.lastIndexOf("/") + 1);
        if (DTOConstraints.KUBE.equals(contextReq) | DTOConstraints.OPENSTACK.equals(contextReq)
                | DTOConstraints.DOCKER.equals(contextReq)) {
            parameter.setValue(contextReq + "/" + nodeKey.substring(nodeKey.lastIndexOf("/") + 1));
        } else {
            parameter.setValue(nodeKey.substring(nodeKey.lastIndexOf("/") + 1));
        }
//        parameter.setParameters(new HashSet<>());
        return parameter;
    }

    private static Set<Property> createProperties(JSONArray jsonArray) {
        Set<Property> properties = new HashSet<>();
        for (Iterator it = jsonArray.iterator(); it.hasNext(); ) {
            JSONObject prop = (JSONObject) it.next();
            String pName = (String) prop.keySet().toArray()[0];
            Object o = prop.get(pName);
            if (o == null) {
                continue;
            }
            JSONObject jObj = ((JSONObject) o);
            Property property = new Property(pName.substring(pName.lastIndexOf("/") + 1));
            Object desc = jObj.get(DTOConstraints.DESC);
            if (desc != null) {
                property.setDescription((String) desc);
            }

            Object values = jObj.get(DTOConstraints.VALUE);
            if (values != null) {
                if (values instanceof JSONArray) {
                    JSONArray pAry = (JSONArray) values;
                    ArrayList<String> list = new ArrayList<>();
                    for (Iterator it2 = pAry.iterator(); it2.hasNext(); ) {
                        list.add((String) it2.next());
                    }
                    property.setValues(list);
                } else {
                    property.setValue(String.valueOf(values));
                }
            }
            JSONObject spec = (JSONObject) jObj.get(DTOConstraints.SPEC);
            if (spec != null) {
                Set<Parameter> parameters = new HashSet<>();
                for (Object keyP : spec.keySet()) {
                    Object specChild = spec.get(keyP);
                    String name = (String) keyP;
                    if (DTOConstraints.GET_INPUT.equals(name)) {
                        Parameter parameter = new Parameter(DTOConstraints.GET_INPUT);
                        if (specChild instanceof JSONArray) {
                            JSONArray pAry = (JSONArray) specChild;
                            parameter.setValue((String) pAry.get(0));
                        } else {
                            parameter.setValue(String.valueOf(specChild));
                        }
                        parameters.add(parameter);
                    } else if (DTOConstraints.GET_PROPERTY.equals(name)) {
                        Parameter parameter = new Parameter(DTOConstraints.GET_PROPERTY);
                        Set<Parameter> children = new HashSet<>();
                        children.add(createParameter((JSONObject) specChild, DTOConstraints.PROPERTY));
                        children.add(createParameter((JSONObject) specChild, DTOConstraints.ENTITY));
                        children.add(createParameter((JSONObject) specChild, DTOConstraints.REQ_CAP));
                        parameter.setParameters(children);
                        parameters.add(parameter);
                    } else {
                        if (specChild instanceof JSONObject) {
                            parameters.add(createParameter((JSONObject) specChild, name));
                        } else {
                            Parameter parameter = new Parameter(name);
                            parameter.setValue(String.valueOf(specChild));
                            parameters.add(parameter);
                        }
                    }
                }
                property.setParameters(parameters);
            }
            if (property.getParameters() == null) {
                property.setParameters(new HashSet<Parameter>());
            }
            properties.add(property);
        }
        return properties;
    }

    private static Set<Attribute> createAttributes(JSONArray jsonArray) {
        Set<Attribute> attributes = new HashSet<>();
        for (Iterator it = jsonArray.iterator(); it.hasNext(); ) {
            JSONObject prop = (JSONObject) it.next();
            String pName = (String) prop.keySet().toArray()[0];
            Object o = prop.get(pName);
            if (o == null) {
                continue;
            }
            JSONObject jObj = ((JSONObject) o);
            Attribute attribute = new Attribute(pName.substring(pName.lastIndexOf("/") + 1));
            Object desc = jObj.get(DTOConstraints.DESC);
            if (desc != null) {
                attribute.setDescription((String) desc);
            }

            Object values = jObj.get(DTOConstraints.VALUE);
            if (values != null) {
                if (values instanceof JSONArray) {
                    JSONArray pAry = (JSONArray) values;
                    ArrayList<String> list = new ArrayList<>();
                    for (Iterator it2 = pAry.iterator(); it2.hasNext(); ) {
                        list.add((String) it2.next());
                    }
                    attribute.setValues(list);
                } else {
                    attribute.setValue(String.valueOf(values));
                }
            }
            JSONObject spec = (JSONObject) jObj.get(DTOConstraints.SPEC);
            if (spec != null) {
                Set<Parameter> parameters = new HashSet<>();

                Object getInput = spec.get(DTOConstraints.GET_INPUT);
                if (getInput != null) {
                    Parameter parameter = new Parameter(DTOConstraints.GET_INPUT);
                    if (getInput instanceof JSONArray) {
                        JSONArray pAry = (JSONArray) getInput;
                        parameter.setValue((String) pAry.get(0));
                    } else {
                        parameter.setValue(String.valueOf(getInput));
                    }
                    parameters.add(parameter);
                }
                Object getProp = spec.get(DTOConstraints.GET_PROPERTY);
                if (getProp != null) {
                    Parameter parameter = new Parameter(DTOConstraints.GET_PROPERTY);
                    Set<Parameter> children = new HashSet<>();
                    children.add(createParameter((JSONObject) getProp, DTOConstraints.PROPERTY));
                    children.add(createParameter((JSONObject) getProp, DTOConstraints.ENTITY));
                    children.add(createParameter((JSONObject) getProp, DTOConstraints.REQ_CAP));
                    parameter.setParameters(children);
                    parameters.add(parameter);
                }
                attribute.setParameters(parameters);
            }
            if (attribute.getParameters() == null) {
                attribute.setParameters(new HashSet<Parameter>());
            }
            attributes.add(attribute);
        }
        return attributes;
    }
}
