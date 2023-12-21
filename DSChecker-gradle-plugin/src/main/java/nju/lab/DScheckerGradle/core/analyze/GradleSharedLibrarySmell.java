package nju.lab.DScheckerGradle.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Has;

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
    HashMap<String, Set<Project>> selfAssignedDeps = new HashMap<>();

    public GradleSharedLibrarySmell(Project project, Map<String, Project> childProjects) {
        this.project = project;
        this.childProjects = childProjects;
    }

    public void getDependenciesOfProject(Project project) {
        File buildScriptFile = project.getBuildFile();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(buildScriptFile));
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
                        String depName = group + ":" + name;
                        selfAssignedDeps.computeIfAbsent(depName, k -> new HashSet<>()).add(project);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void detect() {
        appendToResult("========SharedLibrarySmell========");
        for (Project childProject : project.getAllprojects()) {
            getDependenciesOfProject(childProject);
        }
        for (String dep : selfAssignedDeps.keySet()) {
            if (selfAssignedDeps.get(dep).size() > 1) {
                appendToResult("Dependency " + dep + " is shared by multiple modules but assigned versions individually." );
                for (Project project : selfAssignedDeps.get(dep)) {
                    appendToResult("    " + project.getName());
                }
                appendToResult("---------");
            }
        }
//        getDependenciesOfProject(project);
//        project.getDependencies().getComponents().all()
    }
}

