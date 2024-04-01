package archcheck;

import archcheck.cmd.AnalyzeCommand;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        int exitCode = new CommandLine(new AnalyzeCommand()).execute(args);
        System.out.println();
        System.out.println("Execution time (ms):" + (System.currentTimeMillis() - startTime));
        System.exit(exitCode);
    }
}
