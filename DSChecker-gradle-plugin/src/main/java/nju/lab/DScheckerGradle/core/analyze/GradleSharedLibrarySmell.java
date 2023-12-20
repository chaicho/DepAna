package nju.lab.DScheckerGradle.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GradleSharedLibrarySmell extends BaseSmell {

    Project project;
    Map<String, Project> childProjects;

    public GradleSharedLibrarySmell(Project project, Map<String, Project> childProjects) {
        this.project = project;
        this.childProjects = childProjects;
    }

    public static void parseBuildGradle(File buildGradleFile) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(buildGradleFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("implementation") || line.startsWith("compile")) {
                    // Regular expression to match dependency declaration
                    Pattern pattern = Pattern.compile("[\"']([^:]+):([^:]+):([^'\"]+)[\"']");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String group = matcher.group(1);
                        String name = matcher.group(2);
                        String version = matcher.group(3);
                        System.out.println("Dependency: " + group + ":" + name + ":" + version);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getDependenciesOfProject(Project project) {
        File buildScriptFile = project.getBuildFile();
        parseBuildGradle(buildScriptFile);
//        for (Configuration configuration : project.getConfigurations()) {
//            for (Dependency dependency : configuration.getDependencies()) {
//                log.warn("dependency: {}", dependency);
//            }
//        }
    }
    @Override
    public void detect() {
        appendToResult("========SharedLibrarySmell========");
        for (Project childProject : project.getAllprojects()) {
            getDependenciesOfProject(childProject);
        }
//        getDependenciesOfProject(project);
//        project.getDependencies().getComponents().all()
    }
}

