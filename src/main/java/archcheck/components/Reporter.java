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

package archcheck.components;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static archcheck.Util.sanitizeNodeName;

public class Reporter {
    public static String generateDotGraph(Map<String, Set<String>> dagMap) {
        StringBuilder dotBuilder = new StringBuilder();
        Set<String> nodes = new HashSet<>();

        dotBuilder.append("digraph ServiceDependencyGraph {\n");
        dotBuilder.append("    // Nodes\n");

        // Define nodes with labels
        for (String node : dagMap.keySet()) {
            String sanitizedNode = sanitizeNodeName(node);
            dotBuilder.append("    ").append(sanitizedNode).append(" [label=\"").append(node).append("\"];\n");
            nodes.add(sanitizedNode);
        }

        // Define edges
        dotBuilder.append("\n    // Edges\n");
        for (Map.Entry<String, Set<String>> entry : dagMap.entrySet()) {
            String node = sanitizeNodeName(entry.getKey());
            for (String dependency : entry.getValue()) {
                String sanitizedDependency = sanitizeNodeName(dependency);
                nodes.add(sanitizedDependency);
                dotBuilder.append("    ").append(node).append(" -> ").append(sanitizedDependency)
                        .append(" [label=\"calls\"];\n");
            }
        }

        dotBuilder.append("}\n");

        return dotBuilder.toString();
    }

    public static class GraphDiffResult {
        public Set<String> addedNodes = new HashSet<>();
        public Set<String> removedNodes = new HashSet<>();
        public Set<String> addedEdges = new HashSet<>();
        public Set<String> removedEdges = new HashSet<>();
    }
}
