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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DriftDetector {
    public static Reporter.GraphDiffResult findGraphDifferences(
            Map<String, Set<String>> dependencyMap, Map<String, Set<String>> modifiedEdgeMap) {

        Reporter.GraphDiffResult diffResult = new Reporter.GraphDiffResult();

        // Find added and removed nodes
        for (String node : modifiedEdgeMap.keySet()) {
            if (!dependencyMap.containsKey(node)) {
                diffResult.addedNodes.add(node);
            }
        }
        for (String node : dependencyMap.keySet()) {
            if (!modifiedEdgeMap.containsKey(node)) {
                diffResult.removedNodes.add(node);
            }
        }

        // Find added and removed edges
        for (Map.Entry<String, Set<String>> entry : modifiedEdgeMap.entrySet()) {
            String node = entry.getKey();
            Set<String> edges = entry.getValue();
            Set<String> existingEdges = dependencyMap.getOrDefault(node, Collections.emptySet());

            for (String edge : edges) {
                if (!existingEdges.contains(edge)) {
                    diffResult.addedEdges.add(node + " -> " + edge);
                }
            }
        }
        for (Map.Entry<String, Set<String>> entry : dependencyMap.entrySet()) {
            String node = entry.getKey();
            Set<String> existingEdges = entry.getValue();
            Set<String> modifiedEdges = modifiedEdgeMap.getOrDefault(node, Collections.emptySet());

            for (String edge : existingEdges) {
                if (!modifiedEdges.contains(edge)) {
                    diffResult.removedEdges.add(node + " -> " + edge);
                }
            }
        }

        return diffResult;
    }
}
