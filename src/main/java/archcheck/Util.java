/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
