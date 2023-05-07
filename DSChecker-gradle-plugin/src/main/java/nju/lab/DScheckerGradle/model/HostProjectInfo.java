package nju.lab.DScheckerGradle.model;

import nju.lab.DSchecker.core.model.*;
import org.gradle.api.file.FileCollection;
import soot.SourceLocator;

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
    public String getBuildCp() {
        return classesDirs.getAsPath();
    }

    public void setClassesDirs(FileCollection classesDirs) {
        this.classesDirs = classesDirs;
    }
}
