package nju.lab.DSchecker.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import neu.lab.conflict.container.DepJars;
import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.core.model.IDepJars;
import nju.lab.DSchecker.core.model.IHostProjectInfo;
import soot.SourceLocator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class HostProjectInfo extends IHostProjectInfo {
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

//    void public addABItype


    public void init(ICallGraph callGraph, IDepJars depJars){
       this.callGraph  = callGraph;
       this.depJars = depJars;
    }
    public void addABIClasses(Set<String> ABInames) {
        ABIClasses.addAll(ABInames);
//        System.out.println("ABIClasses: " + ABIClasses);
    }
    public Set<IDepJar> getABIDepJars(){
        Set<IDepJar> depJars = new HashSet<>();
        for(String ABIName : ABIClasses){
            DepJar dep = (DepJar) getSingleUsedDepFromClass(ABIName);
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
