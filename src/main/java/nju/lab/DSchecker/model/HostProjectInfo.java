package nju.lab.DSchecker.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import neu.lab.conflict.container.DepJars;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class HostProjectInfo {
    private static HostProjectInfo instance;

    private HostProjectInfo() {
    }

    public static HostProjectInfo i() {
        if (instance == null) {
            instance = new HostProjectInfo();
        }
        return instance;
    }

    private final Multimap<String, DepJar> usedDependenciesPerClass = ArrayListMultimap.create();

    private File buildDir;
    private String buildPath;


    Set<File> compileSrcFiles ;


    List<String> compileSrcDirs = null;

    public void buildDepClassMap() {
        for (DepJar depJar : DepJars.i().getUsedDepJars()) {
            System.out.println("DepJars: " + depJar.getName());
            for (String className : depJar.getAllCls()) {
                usedDependenciesPerClass.put(className, depJar);
//                System.out.println(className + " " + depJar.getName());
            }
        }
    }

    public Collection<DepJar> getUsedDepFromClass(String className) {
        if(usedDependenciesPerClass.get(className).size() == 0)
            return Collections.emptyList();
        return usedDependenciesPerClass.get(className);
    }
    public DepJar getSingleUsedDepFromClass(String className){
        if(usedDependenciesPerClass.get(className).size() > 1 || usedDependenciesPerClass.get(className).size() == 0)
            return null;
        return usedDependenciesPerClass.get(className).iterator().next();
    }
    public Set<File> getCompileSrcFiles() {
        return compileSrcFiles;
    }
    public void setCompileSrcFiles(Set<File> compileSrcFiles) {
        this.compileSrcFiles = compileSrcFiles;
    }
    public List<String> getCompileSrcDirs() {
        if (compileSrcDirs != null)
            return compileSrcDirs;
        compileSrcDirs = compileSrcFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        return compileSrcDirs;
    }
    public String getCompileSrcCp(){
        return String.join(";", getCompileSrcDirs());
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
        this.buildPath = buildDir.getAbsolutePath();
    }
    public String getBuildCp() {
        return buildPath + File.separator + "classes" + File.separator + "java" + File.separator + "main";
    }
}
