package archcheck.components;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticAnalyzer {

    public static void constructServiceDependencyDAGFromDrawIoXml(Document document,
                                                           Map<String, Set<String>> dependencyMap) {

        Map<String, String> nodeMap = new HashMap<>();
        for (Node node : document.selectNodes("//mxCell")) {
            Element element = (Element) node;
            String nodeId = element.attributeValue("id");
            String nodeValue = element.attributeValue("value");
            String style = element.attributeValue("style");

            if (nodeValue != null) {
                if (nodeValue.isEmpty() && style != null && style.contains("shape=partialRectangle")) {
                    nodeMap.put(nodeId, "external");
                    dependencyMap.put(nodeMap.get(nodeId), new HashSet<>());
                } else if (!nodeValue.trim().isEmpty() && (style == null || !style.contains("shape=hexagon"))){
                    nodeMap.put(nodeId, nodeValue.replaceAll("&nbsp;", " ").trim());
                    dependencyMap.put(nodeMap.get(nodeId), new HashSet<>());
                }
            }
        }
        for (Node node : document.selectNodes("//mxCell")) {
            Element element = (Element) node;
            String source = element.attributeValue("source");
            String target = element.attributeValue("target");

            if (source != null && target != null) {
                dependencyMap.get(nodeMap.get(source)).add(nodeMap.get(target));
            }
        }
    }

    public static void constructServiceDependencyDAGFromComponentYamls (List<Path> componentYamls,
                                                                 Map<String, Set<String>> dependencyMap) {

        dependencyMap.put("external", new HashSet<>());
        for (Path yamlFile : componentYamls) {
            try {
                Map<String, Object> config = loadYaml(yamlFile.toString());

                // Extract metadata
                Map<String, Object> metadata = (Map<String, Object>) config.get("metadata");
                String serviceName = (String) metadata.get("name");

                // Add service to nodeMap
                dependencyMap.put(serviceName, new HashSet<>());

                // Extract outbound service references
                Map<String, Object> spec = (Map<String, Object>) config.get("spec");
                Map<String, Object> outbound = (Map<String, Object>) spec.get("outbound");
                if (outbound != null) {
                    List<Map<String, Object>> serviceReferences = (List<Map<String, Object>>) outbound.get("serviceReferences");
                    if (serviceReferences != null) {
                        for (Map<String, Object> serviceRef : serviceReferences) {
                            String dependentServiceUrl = (String) serviceRef.get("name");
                            String dependentServiceName;
                            dependentServiceName = extractServiceName(dependentServiceUrl);
                            dependencyMap.get(serviceName).add(dependentServiceName);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private static Map<String, Object> loadYaml(String yamlFile) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        return yaml.load(new FileInputStream(yamlFile));
    }

    private static String extractServiceName(String url) {
        Pattern pattern = Pattern.compile("/([^/]+)/[^/]+/?$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find() && !matcher.group(1).startsWith("v")) {
            return matcher.group(1);
        }
        return "external";
    }
}
