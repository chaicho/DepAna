package nju.lab.DScheckerGradle.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class MavenDependencyTreeFetcher {
    public static Map<String, String> getDepVersFromProject(String modulePath, String runtimeClasspath) {

        return null;
    }

    public static void main(String[] args) {
        String projectPath = "/root/dependencySmell/evaluation/happenedSmells/projectsDir/msgraph-sdk-java-core-6110bcfa7bfdc21b20e7244a48c01f1bb8a78569"; // Replace with your project path
        runDependenciesPlugin(projectPath);
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
