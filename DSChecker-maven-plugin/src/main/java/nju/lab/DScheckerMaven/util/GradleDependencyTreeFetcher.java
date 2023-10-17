package nju.lab.DScheckerMaven.util;

import fj.Hash;
import org.apache.maven.project.MavenProject;
import org.gradle.internal.impldep.jcifs.dcerpc.msrpc.LsaPolicyHandle;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleDependencyTreeFetcher {
    static HashMap<String,String> projectDepResult;

    public static String extractDepTreeByConfiguration(String dependenciesOutput, String configurationName) {
        Pattern pattern = Pattern.compile(  configurationName + " - .*(?:\\r\\n|\\n)(.+(?:\\r\\n|\\n))*(?:\\r\\n|\\n)");
        Matcher matcher = pattern.matcher(dependenciesOutput);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }
    public static Map<String, String> extractDepVerFromDepTree(List<String> deps) {
        Map<String, String> ret = new HashMap<>();
        for (String dep : deps) {
            String[] depInfo = dep.split(":");
            if (depInfo.length == 3) {
                String groupId = depInfo[0];
                String artifactId = depInfo[1];
                String version = depInfo[2];
                ret.put(groupId + ":" + artifactId, version);
            }
        }
        return ret;
    }

    public static List<String> extractDepsFromDepTree(String depTree) {
        Pattern pattern = Pattern.compile("^[\\+\\\\]\\-\\-\\- (.*)?",Pattern.MULTILINE);
        // Create a Matcher to find matches
        Matcher matcher = pattern.matcher(depTree);
        List<String> ret = new ArrayList<>();
        while (matcher.find()) {
            String cur = matcher.group(1);
            if (cur.contains("->")) {
                ret.add(cur.split("->")[0].trim());
            }
            else {
                ret.add(cur.trim());
            }
        }
        return ret;
    }
    public static Map<String,String> getDepVersFromConfiguration(String dependenciesOutput, String configurationName) {
        String depTree = extractDepTreeByConfiguration(dependenciesOutput, configurationName);
        List<String> deps = extractDepsFromDepTree(depTree);
        return extractDepVerFromDepTree(deps);
    }
    public static void main(String[] args) {
        String projectPath = "C:\\Users\\DELL\\OneDrive\\dependency-graph-as-task-inputs\\app"; // Replace with your project path
        runDependenciesTask(projectPath);
    }
    public static Map<String,String> getDepVersFromProject(String projectPath) {
        if (!projectDepResult.containsKey(projectPath)) {
            projectDepResult.put(projectPath, runDependenciesTask(projectPath));
        }
        String dependenciesOutput = projectDepResult.get(projectPath);
        return getDepVersFromConfiguration(dependenciesOutput, "runtimeClasspath");
    }

    public static String runDependenciesTask(String projectPath) {

        // Create a Gradle Connector
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File(projectPath));

        // Connect to the Gradle project
        ProjectConnection connection = connector.connect();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            GradleProject project = connection.model(GradleProject.class).get();
            List<String> arguments = new ArrayList<>();
            arguments.add("-q"); // Quiet mode (no logs
//            arguments.add()
            // Run the ":dependencies" task
            connection.newBuild()
                    .forTasks("dependencies")
                    .withArguments(arguments)
                    .setStandardOutput(outputStream)
                    .setStandardError(errorStream)
                    .run();

            // Print the dependencies

            return outputStream.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return "";
    }
}