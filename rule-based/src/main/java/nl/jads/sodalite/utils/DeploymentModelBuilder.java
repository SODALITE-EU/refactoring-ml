package nl.jads.sodalite.utils;


import nl.jads.sodalite.dto.DTOConstraints;
import nl.jads.sodalite.dto.DeploymentModel;
import nl.jads.sodalite.dto.Node;
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

    public static DeploymentModel fromJsonFile(String path) throws IOException, ParseException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        Object obj = new JSONParser().parse(new InputStreamReader(in));

        // typecasting obj to JSONObject
        JSONObject jo = (JSONObject) obj;
        DeploymentModel dm = new DeploymentModel();
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
            if (isNodeTemplate != null && (Boolean) isNodeTemplate) {
                buildNode(jsonObject, dm, key);
            }

        }
        return dm;
    }

    private static void buildDMRoot(JSONObject jsonObject, DeploymentModel dm) {
        dm.setVersion((String) jsonObject.get(DTOConstraints.VERSION));
        dm.setCreatedAt((String) jsonObject.get(DTOConstraints.CREATED_AT));
        dm.setCreatedBy((String) jsonObject.get(DTOConstraints.CREATED_BY));
        dm.setId((String) jsonObject.get(DTOConstraints.ID));
        dm.setNamespace((String) jsonObject.get(DTOConstraints.NAME_SPACE));
        dm.setParticipants((JSONArray) jsonObject.get(DTOConstraints.PARTICIPANTS));
    }

    private static void buildNode(JSONObject jsonObject, DeploymentModel dm, String key) {
        Node node = new Node();
        node.setName(key);
        node.setType((String)jsonObject.get(DTOConstraints.TYPE));
        JSONArray jsonArray = (JSONArray) jsonObject.get(DTOConstraints.PARTICIPANTS);
        if(jsonArray!=null){
            for (Iterator it = jsonArray.iterator(); it.hasNext(); ) {
                Map.Entry pair = (Map.Entry) it.next();
//                String key = (String) pair.getKey();
//                JSONObject jsonObject = (JSONObject) pair.getValue();
//                Property property = new Property()
            }
        }
        dm.addNode(node);
    }
}
