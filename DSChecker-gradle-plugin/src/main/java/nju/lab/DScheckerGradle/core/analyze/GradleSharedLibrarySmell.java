package nju.lab.DScheckerGradle.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.IOException;
import java.util.*;

@Slf4j
public class GradleSharedLibrarySmell extends BaseSmell {

    Project project;
    Map<String, Project> childProjects;

    public GradleSharedLibrarySmell(Project project, Map<String, Project> childProjects) {
        this.project = project;
        this.childProjects = childProjects;
    }
    public void getDependenciesOfProject(Project project) {
        for (Configuration configuration : project.getConfigurations()) {
            for (Dependency dependency : configuration.getDependencies()) {
                log.warn("dependency: {}", dependency);
            }
        }
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

