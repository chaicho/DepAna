package nju.lab.DSchecker.util;


import nju.lab.DSchecker.gradleplugins.tasks.BaseConflictTask;
import nju.lab.DSchecker.model.NodeAdapter;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.Set;

/**
 * A utility class for working with Gradle projects and dependencies.
 * @author swj
 */
public class GradleUtil {


    private static GradleUtil instance;
    private  ArtifactResolutionQuery artifactResolutionQuery;

    private GradleUtil() {
    }

    public static GradleUtil i() {
        return instance;
    }
    public static void init(BaseConflictTask task) {
      if(instance == null) {
        instance = new GradleUtil(task);
      }
    }
    private GradleUtil(BaseConflictTask task) {
        this.task = task;
        this.project = task.project;
        this.dependencyHandler = project.getDependencies();
        this.artifactResolutionQuery = dependencyHandler.createArtifactResolutionQuery();
    }
    private BaseConflictTask task;
    private Project project;
    private DependencyHandler dependencyHandler;


    private Set<String> hostClasses;
    /**
         * use soot to get host classes
         * @return Set<String> hostClses
     */
    static int resolvedArtifactCount = 0;
    public Set<ResolvedArtifact> resolveArtifact(String  displayName){


//            Configuration config = project.getConfigurations().detachedConfiguration();
            Configuration config = project.getConfigurations().create("Download" + resolvedArtifactCount++);
            config.setTransitive(false); // if required
            project.getDependencies().add(config.getName(), displayName);
//          project.getDependencies().add(config.getName(), "org.apache.commons:commons-collections4:4.2");
//            File file = config.getSingleFile();
//            System.out.println(file.getAbsolutePath());
            return config.getResolvedConfiguration().getResolvedArtifacts();
    }

    public void resolveArtifact(String  displayName, NodeAdapter nodeAdapter){


//            Configuration config = project.getConfigurations().detachedConfiguration();
        Configuration config = project.getConfigurations().create("Download" + resolvedArtifactCount++);
        config.setTransitive(false); // if required
        project.getDependencies().add(config.getName(), displayName);

        ResolvedConfiguration resolvedConfig = config.getResolvedConfiguration();

        File file = config.getSingleFile();
        nodeAdapter.setArtifacts(resolvedConfig.getResolvedArtifacts());
//        System.out.println(file.getAbsolutePath());
    }
//    public Set<String> getHostClasses() {
//        if (hostClasses == null) {
//            hostClasses = new HashSet<>();
//            for (SourceSet sourceSet : task.getProject().getSourceSets()) {
//                for (File file : sourceSet.getAllJava().getSrcDirs()) {
//                    hostClasses.addAll(task.getSootClses(file));
//                }
//            }
//        }
//        return hostClasses;
//    }





    /**
     * Gets the logger for the current Gradle project.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return task.getLogger();
    }

    public File getBuildDir() {
        return  task.buildDir;
    }


    /**
     * 判断nodeAdapter是否是Host项目
     * @param nodeAdapter
     * @return
     */
    public boolean isHost(NodeAdapter nodeAdapter) {
//        return task.getRoot().getId().getDisplayName();
        return false;
    }

    /**
     * Gets the set of all Java classes in the project's source directories.
     *
     * @return the set of Java classes
     */

    /**
     * Gets the set of Java classes in the given directory.
     *
     * @param classesDir the directory to scan for classes
     * @param output     the output directory corresponding to the given classes directory
     * @return the set of Java classes
     */



}