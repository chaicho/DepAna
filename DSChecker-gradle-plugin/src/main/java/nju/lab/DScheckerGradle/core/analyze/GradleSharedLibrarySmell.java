package nju.lab.DScheckerGradle.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;

import org.gradle.api.Project;

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

    @Override
    public void detect() {
        appendToResult("========SharedLibrarySmell========");

    }
}

