package archcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static List<Path> getYamlFileList(String componentsDir) throws IOException {
        List<Path> yamlFiles = new ArrayList<>();
        Files.walk(Paths.get(componentsDir))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith("component_config.yaml"))
                .forEach(yamlFiles::add);
        return yamlFiles;
    }

    // Sanitizes the node name by replacing spaces with underscores
    public static String sanitizeNodeName(String nodeName) {
        return nodeName.replace(" ", "_").replace("-", "_");
    }
}
