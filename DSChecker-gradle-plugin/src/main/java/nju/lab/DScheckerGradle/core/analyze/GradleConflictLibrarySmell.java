package nju.lab.DScheckerGradle.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.util.*;
@Slf4j
public class GradleConflictLibrarySmell extends BaseSmell {
    Project project;
    Map<String, Project> childProjects;
    Map<String, Set<String>> depVersionMap = new HashMap<>();
    public GradleConflictLibrarySmell(Project project, Map<String, Project> childProjects) {
        this.project = project;
        this.childProjects = childProjects;
    }
    public void getDependenciesOfProject(Project project) {
        for (Configuration configuration : project.getConfigurations()) {
            for (Dependency dependency : configuration.getDependencies()) {
                log.warn("dependency: {}", dependency);
                depVersionMap.computeIfAbsent(dependency.getName(), k -> new HashSet<>()).add(dependency.getVersion());
            }
        }
    }
    @Override
    public void detect() {
        appendToResult("========ConflictLibrarySmell========");
        for (Project childProject : project.getAllprojects()) {
            getDependenciesOfProject(childProject);
        }
        for (String dep : depVersionMap.keySet()) {
            if (depVersionMap.get(dep).size() > 1) {
                appendToResult("Dependency " + dep + " has inconsistent versions between modules." );
                appendToResult("    " + depVersionMap.get(dep));
                appendToResult("---------");
            }
        }
    }
}
