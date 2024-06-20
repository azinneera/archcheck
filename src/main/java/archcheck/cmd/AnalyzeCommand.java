package archcheck.cmd;

import archcheck.Util;
import archcheck.components.DriftDetector;
import archcheck.components.Reporter;
import archcheck.components.StaticAnalyzer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "analyze", description = "Analyze the architecture drift")
public class AnalyzeCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--intended", description = "Intended architecture as a drawio file.", required = true)
    private String refDrawIoXml;

    @CommandLine.Option(names = "--modified", description = "Component.yaml files of implemented architecture.")
    private String componentsDir;

    @CommandLine.Option(names = "--dump-graphs",
            description = "Print the original and modified dependency graphs in DOT graph text format")
    private boolean dumpGraphs;

    PrintStream outStream = System.out;

    @Override
    public Integer call() throws DocumentException, IOException {
        // 1. Read the reference architecture file
        SAXReader reader = new SAXReader();
        Document document = reader.read(refDrawIoXml);

        // 2. Construct the DAG of service dependencies according to the reference architecture
        outStream.println("\n\u001B[32mAnalyzing the intended architecture...\u001B[0m\n");
        Map<String, Set<String>> dependencyMap = new HashMap<>();

        StaticAnalyzer.constructServiceDependencyDAGFromDrawIoXml(document, dependencyMap);

        // 3. Dump the reference service dependency graph (if asked for)
        if (dumpGraphs) {
            String depGraph = Reporter.generateDotGraph(dependencyMap);
            outStream.println(depGraph);
            Files.write(Paths.get("intended.dot"), depGraph.getBytes(), StandardOpenOption.CREATE);
        }

        // 4. Construct the modified architecture using component yaml files
        outStream.println("\u001B[32mGenerating the implemented architecture...\u001B[0m\n");

        Map<String, Set<String>> modifiedEdgeMap = new HashMap<>();
        List<Path> yamlFileList = Util.getYamlFileList(componentsDir);
        StaticAnalyzer.constructServiceDependencyDAGFromComponentYamls(yamlFileList, modifiedEdgeMap);

        // 5. Dump the reference service dependency graph (if asked for)
        if (dumpGraphs) {
            String depGraph = Reporter.generateDotGraph(modifiedEdgeMap);
            outStream.println(depGraph);
            Files.write(Paths.get("modified.dot"), depGraph.getBytes(), StandardOpenOption.CREATE);
        }

        // 6. Generate the diff of the graphs
        outStream.println("\u001B[32mAnalyzing deviations...\u001B[0m\n");
        // Find the differences between the two graphs
        Reporter.GraphDiffResult diffResult = DriftDetector.findGraphDifferences(dependencyMap, modifiedEdgeMap);

        // 7. Analyze and report the drifts
        outStream.println("\u001B[32mGenerating the drift summary...\u001B[0m\n");

        // Print the differences
        System.out.println("Diverged nodes: " + diffResult.addedNodes);
        System.out.println("Diverged edges: " + diffResult.addedEdges);
        System.out.println("Absent nodes: " + diffResult.removedNodes);
        System.out.println("Absent edges: " + diffResult.removedEdges);

        if (!diffResult.addedNodes.isEmpty() || !diffResult.addedEdges.isEmpty() ||
                !diffResult.removedNodes.isEmpty() || !diffResult.removedEdges.isEmpty()) {
            return 1;
        }

        return 0;
    }
}
