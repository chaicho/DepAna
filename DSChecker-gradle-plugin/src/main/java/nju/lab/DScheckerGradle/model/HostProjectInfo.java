package nju.lab.DScheckerGradle.model;

import nju.lab.DSchecker.core.model.*;
import org.gradle.api.file.FileCollection;
import soot.SourceLocator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class HostProjectInfo extends IHostProjectInfo {
    private static HostProjectInfo instance;
    private Set<String> apiDepJars = new HashSet<>();
    private FileCollection classesDirs;

    private HostProjectInfo() {
    }

    public static HostProjectInfo i() {
        if (instance == null) {
            instance = new HostProjectInfo();
        }
        return instance;
    }



    @Override
    public void buildDepClassMap() {
        super.buildDepClassMap();
        for ( String compileSrcDir: compileSrcDirs){
            System.out.println(compileSrcDir);
            consumerClasses.addAll(SourceLocator.v().getClassesUnder(compileSrcDir)
                    .stream()
                    .map(ClassVO::new)
                    .collect(Collectors.toSet()));
            consumerClasses.forEach(consumerClass -> {
                System.out.println("consumerClass: " + consumerClass.getClsSig());
            });
        }
    }
    @Override
    public String getWrapperPath(){
        return rootDir.getPath() + File.separator + "gradle" + File.separator + "wrapper";
    }

    /**
     * Get the compiled classes path of the sources of the project, separated by commas.
     * @return
     */
    @Override
    public String getBuildCp() {
        return classesDirs.getAsPath();
    }

    /**
     * Set the compiled classes path of the sources of the project.
     * @param classesDirs
     */
    public void setClassesDirs(FileCollection classesDirs) {
        this.classesDirs = classesDirs;
    }
    @Override
    public List<String> getHostClasses(){
        if(hostClasses == null) {
            hostClasses = SourceLocator.v().getClassesUnder(getBuildCp());
        }
        return hostClasses;
    }

}
