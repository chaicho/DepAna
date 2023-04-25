package nju.lab.DSchecker.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import neu.lab.conflict.container.DepJars;
import soot.SourceLocator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class HostProjectInfo {
    private static HostProjectInfo instance;
    private Set<String> apiDepJars = new HashSet<>();

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
//    compileSrcFiles represents the source files of the project;
    private Set<File> compileSrcFiles ;
    //   compileSrcDirs represents the source paths of the project;
    private Set<String> compileSrcDirs = new HashSet<>();
    private Set<ClassVO> consumerClasses = new HashSet<>();

    private Set<String> ABIClasses = new HashSet<>();

    public void buildDepClassMap() {
        for (DepJar depJar : DepJars.i().getUsedDepJars()) {
            System.out.println("DepJars: " + depJar.getName());
            for (String className : depJar.getAllCls()) {
                usedDependenciesPerClass.put(className, depJar);
//                System.out.println(className + " " + depJar.getName());
            }
        }
        for ( String compileSrcDir: compileSrcDirs){
            consumerClasses.addAll(SourceLocator.v().getClassesUnder(compileSrcDir)
                    .stream()
                    .map(ClassVO::new)
                    .collect(Collectors.toSet()));
            consumerClasses.forEach(consumerClass -> {
                System.out.println("consumerClass: " + consumerClass.getClsSig());
            });
        }

    }
    public Collection<String> getDuplicateClassNames(){
        return usedDependenciesPerClass.keySet()
                .stream()
                .filter(className -> usedDependenciesPerClass.get(className).size() > 1)
                .collect(Collectors.toList());
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
        this.compileSrcDirs = compileSrcFiles.stream()
                                             .map(File::getAbsolutePath)
                                             .collect(Collectors.toSet());
    }
    public Set<String> getCompileSrcDirs() {

        return compileSrcDirs;
    }
    public String getCompileSrcCp(){
        return String.join(";", getCompileSrcDirs());
    }
//    void public addABItype
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
        this.buildPath = buildDir.getAbsolutePath();
    }
    public String getBuildCp() {
        return buildPath + File.separator + "classes" + File.separator + "java" + File.separator + "main";
    }

    public void addABIClasses(Set<String> ABInames) {
        ABIClasses.addAll(ABInames);
//        System.out.println("ABIClasses: " + ABIClasses);
    }
    public Set<DepJar> getABIDepJars(){
        Set<DepJar> depJars = new HashSet<>();
        for(String ABIName : ABIClasses){
            DepJar dep = getSingleUsedDepFromClass(ABIName);
            if(dep != null)
                depJars.add(dep);
        }
        return depJars;
    }

    public void setApiDepJars(Set<String> apiArtifacts) {
        this.apiDepJars = apiArtifacts;
        System.out.println(apiArtifacts);
    }
    public Set<String> getApiDepJars() {
        return apiDepJars;
    }

}
