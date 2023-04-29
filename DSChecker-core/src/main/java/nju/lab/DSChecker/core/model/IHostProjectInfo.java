package nju.lab.DSchecker.core.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class IHostProjectInfo  {

    protected IDepJars depJars;
    protected ICallGraph callGraph;
    private File buildDir;

    private String buildPath;
    //    compileSrcFiles represents the source files of the project;
    private Set<File> compileSrcFiles ;
    //   compileSrcDirs represents the source paths of the project;
    private Set<String> compileSrcDirs = new HashSet<>();
    private final Multimap<String, IDepJar> usedDependenciesPerClass = ArrayListMultimap.create();

    public abstract void init(ICallGraph callGraph, IDepJars depJars);

    /**
     * Construct the class to Depjar map.
     * @param
     * @return
     */
    public void buildDepClassMap() {
        for (IDepJar depJar : depJars.getUsedDepJars()) {
            System.out.println("DepJars: " + depJar.getName());
            for (String className : depJar.getAllCls()) {
                usedDependenciesPerClass.put(className, depJar);
            }
        }
    }

    /**
     * Get the duplicate class names.
     * @return the class names that are used by more than one jar.
     */
    public Collection<String> getDuplicateClassNames(){
        return usedDependenciesPerClass.keySet()
                .stream()
                .filter(className -> usedDependenciesPerClass.get(className).size() > 1)
                .collect(Collectors.toList());
    }

    /**
     * Get the used Depjar that a class belongs to.
     * @param className
     * @return The actual used Depjar that a class belongs to.
     */
    public Collection<IDepJar> getUsedDepFromClass(String className) {
        if(usedDependenciesPerClass.get(className).size() == 0)
            return Collections.emptyList();
        return usedDependenciesPerClass.get(className);
    }

    /**
     * Get the single used Depjar that a class belongs to since there are multiple classes with the same name.
     * @param className
     * @return The actual used Depjar that a class belongs to.
     */
    public  IDepJar getSingleUsedDepFromClass(String className){
        if(usedDependenciesPerClass.get(className).size() > 1 || usedDependenciesPerClass.get(className).size() == 0)
            return null;
        return usedDependenciesPerClass.get(className).iterator().next();
    }

    /**
     * Get the class name from the class file path.
     * @return
     */
    public Set<File> getCompileSrcFiles() {
        return compileSrcFiles;
    }

    public void setCompileSrcFiles(Set<File> compileSrcFiles) {
        this.compileSrcFiles = compileSrcFiles;
        this.compileSrcDirs = compileSrcFiles.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
        this.buildPath = buildDir.getAbsolutePath();
    }
    public String getBuildCp() {
        return buildPath + File.separator + "classes" + File.separator + "java" + File.separator + "main";
    }

    /**
     * Get all the jar files reachable by the host project.
     * @return Set of jar files
    */
    public  Set<IDepJar> getReachableJars(){
        Set<String> reachableClasses = callGraph.getReachableClasses();
        Set<IDepJar> ret = new java.util.HashSet<>();
        for (String className : reachableClasses) {
            IDepJar depJar = getSingleUsedDepFromClass(className);
            if (depJar != null) {
                ret.add(depJar);
            }
        }
        return ret;
    }

}
