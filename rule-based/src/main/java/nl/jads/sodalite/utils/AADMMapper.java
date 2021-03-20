package nl.jads.sodalite.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import tosca.mapper.dto.*;
import tosca.mapper.exchange.generator.AADMGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class AADMMapper {
    private static final Logger LOG = LoggerFactory.getLogger(AADMMapper.class.getName());
    Yaml yaml;
    String inputModel;
    Set<Node> nodes = new HashSet<>();
    Set<Node> nodeTemplates = new HashSet<>();
    Set<Property> inputs = new HashSet<>();
    Set<String> imports = new HashSet<>();
    public AADMMapper(String inputModel) {
        yaml = new Yaml();
        this.inputModel = inputModel;
    }

    private void Expect(boolean guard) {
        if (!guard)
            throw new ParseError();
    }

    public void parse() throws IOException {
        Map<?, ?> map = (Map) yaml.load(inputModel);
        if (null == map) {
            throw new ParseError();
        }
        LOG.info("data: {}", map.toString());
        parseDocument(map);
    }

    public void parseDocument(Map<?, ?> map) {
        map.forEach((k, v) -> {
            LOG.info("k: {}, v: {}", k, v);
            String key = k.toString();
            switch (key) {
                case "import":
                    imports.add(String.valueOf(v));
                    break;
                case "module":
                    break;
                case "node_templates":
                    Set<Node> templatesPerCategory = parseTemplates(v);
                    nodeTemplates.addAll(templatesPerCategory);
                    break;
                case "inputs":
                    inputs = parseProperties(v);
                    break;
                default:
                    throw new ParseError();
            }
        });

    }

    public Set<Node> parseTemplates(Object value) {
        Set<Node> nodes = new HashSet<>();
        System.err.println("parseTemplates");
        Map<?, ?> typeMap = (Map<?, ?>) value;
        typeMap.forEach((k, v) -> {
            String name = k.toString();
            Node node = new Node(name);
            Map<?, ?> descriptionMap = (Map<?, ?>) v;
            descriptionMap.forEach((k2, v2) -> {
                String key2 = k2.toString();
                String value2 = v2.toString();
                switch (key2) {
                    case "type":
                        node.setOfType(value2);
                        break;
                    case "description":
                        node.setDescription(value2);
                        break;
                    case "attributes":
                        Set<Attribute> attributes = parseAttributes(v2);
                        node.setAttributes(attributes);
                        break;
                    case "properties":
                        Set<Property> properties = parseProperties(v2);
                        node.setProperties(properties);
                        break;
                    case "requirements":
                        Set<Requirement> requirements = parseRequirements(v2);
                        node.setRequirements(requirements);
                        break;
                    case "capabilities":
                        Set<Capability> capabilities = parseCapabilities(v2);
                        node.setCapabilities(capabilities);
                        break;
                    default:
                        throw new ParseError();
                }
            });
            nodes.add(node);
        });
        return nodes;
    }

    public Set<Attribute> parseAttributes(Object map) {
        Set<Attribute> attributes = new HashSet<>();
        Map<?, ?> attrMap = (Map<?, ?>) map;
        attrMap.forEach((k, v) -> {
            Attribute p = new Attribute(k.toString());
            //attributes.add(p);
            valueOrParameter(v, p, attributes);
        });

        return attributes;
    }

    public Set<Property> parseProperties(Object map) {
        Set<Property> properties = new HashSet<>();
        Map<?, ?> propMap = (Map<?, ?>) map;
        propMap.forEach((k, v) -> {
            Property p = new Property(k.toString());
            //properties.add(p);
            String description = null;
            if (v instanceof Map) {
                Map<String, String> vMap = (Map<String, String>) v;
                if (vMap.containsKey("description"))
                    p.setDescription(vMap.get("description"));
            }

            if (description == null)
                valueOrParameter(v, p, properties);
        });

        return properties;
    }

    public Set<Requirement> parseRequirements(Object map) {
        Set<Requirement> requirements = new HashSet<>();
        ArrayList<?> reqList = (ArrayList<?>) map;
        reqList.forEach((req) -> {
            Map<?, ?> reqMap = (Map<?, ?>) req;
            reqMap.forEach((k, v) -> {
                Requirement p = new Requirement(k.toString());
                //requirements.add(p);
                valueOrParameter(v, p, requirements);
            });
        });

        return requirements;
    }

    public Set<Capability> parseCapabilities(Object map) {
        Set<Capability> capabilities = new HashSet<>();
        Map<?, ?> capMap = (Map<?, ?>) map;
        capMap.forEach((k, v) -> {
            Capability cap = new Capability(k.toString());
            //capabilities.add(cap);
            valueOrParameter(v, cap, capabilities);
        });

        return capabilities;
    }

    public Set<Parameter> parseParameters(Object map) {
        Set<Parameter> parameters = new HashSet<>();
        Map<?, ?> paramMap = (Map<?, ?>) map;
        paramMap.forEach((k, v) -> {
            Parameter p = new Parameter(k.toString());
            valueOrParameter(v, p, parameters);
        });
        return parameters;
    }

    public <T> void valueOrParameter(Object v, Parameter p, Set<T> concepts) {
        Set<Parameter> parameters = new HashSet<>();
        if (v instanceof Map) {
            p.setParameters(parseParameters(v));
        } else if (v instanceof ArrayList) {
            //constraints is a list
            ArrayList<?> list = (ArrayList<?>) v;
            LOG.info("ARRAYLIST: {} ", v);
            list.forEach((l) -> {
                if (l instanceof Map) {

                    Map<?, ?> paramMap = (Map<?, ?>) l;
                    LOG.info("MAP: {}", l);
                    paramMap.forEach((key, val) -> {
                        Parameter p2 = new Parameter(key.toString());
                        if (val instanceof ArrayList) {
                            p2.setValues((ArrayList<String>) val);
                        } else {
                            p2.setValue(val.toString());
                        }
                        parameters.add(p2);
                    });
                }
            });
            p.setParameters(parameters);
        } else {
            p.setValue(v.toString());
            p.setParameters(parameters);
        }

        concepts.add((T) p);
    }

    public String getExchangeAADM() {
        if (!nodeTemplates.isEmpty()) {
            AADMGenerator aadm = new AADMGenerator(nodeTemplates, inputs);
            aadm.convertModelToExchange();
            return aadm.getExchangeModel();
        }
        return null;
    }

    private class ParseError extends RuntimeException {
    }
}
