package nju.lab.DScheckerGradle.util;

import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Has;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenDependencyTreeFetcher {
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("([^:]+):([^:]+):[^:]+:([^:]+):[^:]+");

    public static Map<String, String> getDepVersFromProject(String modulePath) {
        String dependencyTree = runDependenciesPlugin(modulePath);
        if (dependencyTree != null) {
            return parseDependencyTree(dependencyTree);
        }
        return null;
    }
    // A method that takes a dependency tree as a string and returns a map of GA to version
    public static Map<String, String> parseDependencyTree(String dependencyTree) {
        // Create an empty map to store the result
        Map<String, String> map = new HashMap<>();

        // Split the dependency tree by line separator
        String[] lines = dependencyTree.split(System.lineSeparator());
        // Loop through each line
        for (String line : lines) {
            // Remove the [INFO] prefix and any leading or trailing whitespace
            line = line.replace("[INFO]", "");
            // System.out.println(line);
            if (!line.startsWith(" +-") && !line.startsWith(" \\-")) {
                continue;
            }
            line = line.trim();
            line = line.split(" ")[1];
            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }

            // Remove the +- symbols from the line
//            line = line.replaceAll("[+-|]", "");
//            line = line.trim();
            // Match the line with the dependency pattern
            Matcher matcher = DEPENDENCY_PATTERN.matcher(line);

            // If the line matches the pattern, extract the GA and version and put them in the map
            if (matcher.find()) {
                String groupId = matcher.group(1);
                String artifactId = matcher.group(2);
                String version = matcher.group(3);
                String ga = groupId + ":" + artifactId;
                map.put(ga, version);
            }
        }

        // Return the map
        return map;
    }

    public static void main(String[] args) {
        String projectPath = "/home/chaicho/PathToDoc/DependencySmell/Detection/DepAna"; // Replace with your project path
        HashMap<String, String> depVers = (HashMap<String, String>) getDepVersFromProject(projectPath);
        for (String dep : depVers.keySet()) {
            System.out.println(dep);
            System.out.println(depVers.get(dep));
        }
    }

    private static String runDependenciesPlugin(String projectPath) {
        ProcessBuilder processBuilder = new ProcessBuilder("mvn", "dependency:tree");
        processBuilder.directory(new File(projectPath));
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Gradle task execution successful.");
                System.out.println(output);
                return output.toString();
            } else {
                System.err.println("Gradle task execution failed with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

         return "";
    }
}
