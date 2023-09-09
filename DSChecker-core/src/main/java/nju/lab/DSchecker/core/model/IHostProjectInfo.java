package nju.lab.DSchecker.core.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import nju.lab.DSchecker.util.javassist.GetRefedClasses;
import nju.lab.DSchecker.util.source.analyze.FullClassExtractor;
import soot.SourceLocator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class IHostProjectInfo  {

    protected IDepJars<? extends IDepJar> depJars;
    protected ICallGraph callGraph;
    protected File buildDir;

    protected File rootDir;

    protected File outputFile;
    protected String buildPath;
    /**
    compileSrcFiles represents the source files of the project;
    */
    protected Set<File> compileSrcFiles ;
    /**
     * testCompileSrcFiles represents the test source files of the project;
     */
    protected Set<File> testCompileSrcFiles;
    /**
     * compileSrcDirs represents the source paths of the project;
     */
    protected final Multimap<String, IDepJar> usedDependenciesPerClass = ArrayListMultimap.create();

    protected Set<String> compileSrcDirs = new HashSet<>();

    protected Set<ClassVO> consumerClasses = new HashSet<>();

    protected Set<String> ABIClasses = new HashSet<>();
    protected Set<String> apiDepJars = new HashSet<>();
    protected List<String> hostClasses;
    private Set<String> testCompileSrcDirs;

    public Set<String> referencedClasses = new HashSet<>();

    private Set<IDepJar> actualCompileDepJars = new HashSet<>();

    private Set<IDepJar> actualTestDepJars = new HashSet<>();

    private Set<IDepJar> actualRuntimeDepJars = new HashSet<>();


    /**
     * Construct the class to Depjar map.
     * @param
     * @return
     */
    public void buildDepClassMap() {
        for (IDepJar depJar : depJars.getSeqUsedDepJars()) {
            for (String className : depJar.getAllCls()) {
                usedDependenciesPerClass.put(className, depJar);
            }
        }
    }

    /**
     * Set the root directory of the project.
      * @param dir
     */
     public void setRootDir(File dir){
         this.rootDir = dir;
     }

    /**
     * Get the root directory of the project.
     * @return
     */
    public File getRootDir(){
         return rootDir;
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


    public List<String> getHostClasses(){
        if(hostClasses == null) {
            hostClasses = SourceLocator.v().getClassesUnder(getBuildCp());
        }
        return hostClasses;
    }

    /**
     * Get the used Depjar that a class belongs to.·
     * @param className
     * @return The actual used Depjar that a class belongs to.
     */
    public Collection<IDepJar> getUsedDepFromClass(String className) {
        if(usedDependenciesPerClass.get(className).size() == 0)
            return Collections.emptyList();
        return usedDependenciesPerClass.get(className);
    }

    public IDepJar getFirstUsedDepFromClass(String className) {
        if(usedDependenciesPerClass.get(className).size() == 0)
            return null;
        return usedDependenciesPerClass.get(className).iterator().next();
    }

    public IDepJar getFirstUsedDepFromClassWithTargetScene(String className, String scene) {
        if(usedDependenciesPerClass.get(className).size() == 0)
            return null;
        Set<String> appropriateScopes = new HashSet<String>();
        if (scene == "compile") {
            appropriateScopes.add("compile");
            appropriateScopes.add("provided");
            appropriateScopes.add("runtime");
        }
        else if (scene == "runtime") {
            appropriateScopes.add("runtime");
            appropriateScopes.add("compile");
        }
        else if (scene == "test") {
            appropriateScopes.add("test");
            appropriateScopes.add("compile");
            appropriateScopes.add("provided");
            appropriateScopes.add("runtime");
        }
        for (IDepJar depJar : usedDependenciesPerClass.get(className)) {
            if(appropriateScopes.contains(depJar.getScope()))
                return depJar;
        }
        return null;
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
    public Set<String> getCompileSrcDirs() { return compileSrcDirs;}
    public Set<File> getTestCompileSrcFiles() {
        return testCompileSrcFiles;
    }

    public Set<String> getTestCompileSrcDirs() {
        return testCompileSrcDirs;
    }
    public void setTestCompileSrcPaths(List<String> paths) {
        this.testCompileSrcDirs = paths.stream().map(String::trim).collect(Collectors.toSet());
        this.testCompileSrcFiles = testCompileSrcDirs.stream()
                .map(File::new)
                .collect(Collectors.toSet());
    }
    public void setCompileSrcFiles(Set<File> compileSrcFiles) {
        this.compileSrcFiles = compileSrcFiles;
        this.compileSrcDirs = compileSrcFiles.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }
    
    public void setTestCompileSrcFiles(Set<File> testCompileSrcFiles) {
        this.testCompileSrcFiles = testCompileSrcFiles;
        this.testCompileSrcDirs = testCompileSrcFiles.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toSet());
    }


    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
        this.buildPath = buildDir.getAbsolutePath();
        this.outputFile =new File(buildDir, "DScheckerResult.txt");
        if(!outputFile.exists()){
            try {
                outputFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Get the  path of the host project which contains the .classes files.
     * @return
     */
    abstract public String getBuildCp();

    /**
     * Get all the jar files reachable by the host project.
     * @return Set of jar files
    */
    public Set<IDepJar> getRuntimeDirectReachableJars() {
        Set<String> reachableClasses = new HashSet<>(callGraph.getReachableDirectClasses());
        Set<String> constantPoolClasses =  GetRefedClasses.analyzeReferencedClasses(getBuildCp());
        reachableClasses.addAll(constantPoolClasses);
        Set<IDepJar> ret = new java.util.HashSet<>();
        for (String className : reachableClasses) {
            IDepJar depJar = getFirstUsedDepFromClassWithTargetScene(className,"runtime");
            if (depJar != null) {
                ret.add(depJar);
                depJar.addClassToScene("runtime", className);
                System.out.println(className + " is reachable by methods");
                System.out.println(callGraph.getSourceMethods(className));
                System.out.println(depJar.getDisplayName());
            }
        }
        return ret;
    }
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

    public void init(ICallGraph callGraph, IDepJars depJars){
        this.callGraph  = callGraph;
        this.depJars = depJars;
    }
    public void initABIDepjars(Set<String> ABInames) {
        ABIClasses.addAll(ABInames);
        System.out.println("=========ABI========");
        System.out.println(ABIClasses);
    }
    public Set<IDepJar> getABIDepJars(){
        Set<IDepJar> depJars = new HashSet<>();
        for(String ABIName : ABIClasses){
            IDepJar dep = getSingleUsedDepFromClass(ABIName);
            if(dep != null)
                depJars.add(dep);
        }
        return depJars;
    }
    public Set<String> getABIClasses(){
        return ABIClasses;
    }
    public void setApiDepJars(Set<String> apiArtifacts) {
        this.apiDepJars = apiArtifacts;
//        System.out.println(apiArtifacts);
    }
    public Set<String> getApiDepJars() {
        return apiDepJars;
    }

    public File getOutputFile(){
        return outputFile;
    }

    abstract public String getWrapperPath();

    public abstract String getBuildTestCp();

    public abstract String getBuildTool();



    public Set<IDepJar> getActualDepJarsUsedAtScene(String scene) {
        if(scene.equals("compile")) {
            if (actualCompileDepJars.isEmpty()) {
                Set<String> referencedClassesInSrcCode = getReferencedClassesFromSrc();
                //  Import classes are not all classes for some classes within the same package do not need to be imported.
                Set<IDepJar> depJars = new HashSet<>();
                for (String referencedClass : referencedClassesInSrcCode) {
                    IDepJar depJar = getFirstUsedDepFromClassWithTargetScene(referencedClass, "compile");
                    if (depJar != null && !depJar.isHost()) {
                        depJars.add(depJar);
                        depJar.addClassToScene("compile", referencedClass);
                    }
                }
                actualCompileDepJars = depJars;
            }
            return new HashSet<>(actualCompileDepJars);
        }
        else if (scene.equals("test")) {
            if (actualTestDepJars.isEmpty()) {
                Set<String> referencedClassesInByteCode =  GetRefedClasses.analyzeReferencedClasses(getBuildTestCp());
                Set<String> referencedClassesInSrcCode =  getReferencedClassesFromTestSrc();
                Set<String> allReferencedClasses = new HashSet<>(referencedClassesInByteCode);
                allReferencedClasses.addAll(referencedClassesInSrcCode);
                Set<IDepJar> depJars = new HashSet<>();
                for (String referencedClass : allReferencedClasses) {
                    IDepJar depJar = getFirstUsedDepFromClassWithTargetScene(referencedClass, "test");
                    if (depJar != null && !depJar.isHost()) {
                        depJars.add(depJar);
                        depJar.addClassToScene("test", referencedClass);
                    }
                }
                actualTestDepJars = depJars;
            }
            return new HashSet<>(actualTestDepJars);
        }
        else if (scene.equals("runtime")) {
            if (actualRuntimeDepJars.isEmpty()) {
                actualRuntimeDepJars = getRuntimeDirectReachableJars();
            }
            return new HashSet<>(actualRuntimeDepJars);
        }
        return new HashSet<>();
    }

    public Set<String> getReferencedClassesFromSrc() {
        Set<String> ret = new HashSet<>();
        for (String compileSrcPath : getCompileSrcDirs() ) {
            ret.addAll(FullClassExtractor.getClassesFromJavaFiles(compileSrcPath));
        }
        return ret;
    }

    /**
     * Get all the classes referenced by the host project's bytecode.
     * @return
     */
    public Set<String> getReferencedClassesFromBuild() {
        Set<String> ret = new HashSet<>();
        ret.addAll(GetRefedClasses.analyzeReferencedClasses(getBuildCp()));
        return ret;
    }
    /**
     * Get all the classes referenced by the host project's tests' bytecode.
     * @return
     */
    public Set<String> getReferencedClassesFromTestBuild() {
        Set<String> ret = new HashSet<>();
        ret.addAll(GetRefedClasses.analyzeReferencedClasses(getBuildTestCp()));
        return ret;
    }

    /**
     * Get all the classes referenced by the host project, including src code and test code.
     * @return
     */
    public Set<String> getReferencedClassesFromAll() {
        if (!referencedClasses.isEmpty()) {
            return referencedClasses;
        }
        referencedClasses.addAll(getReferencedClassesFromSrc());
        referencedClasses.addAll(getReferencedClassesFromBuild());
        referencedClasses.addAll(getReferencedClassesFromTestSrc());
        referencedClasses.addAll(getReferencedClassesFromTestBuild());
        return referencedClasses;
    }

    public Set<String> getReferencedClassesFromTestSrc() {
        Set<String> ret = new HashSet<>();
        for (String compileSrcPath : getTestCompileSrcDirs() ) {
            ret.addAll(FullClassExtractor.getClassesFromJavaFiles(compileSrcPath));
        }
        return ret;
    }

    public boolean isUsedByHost(String className) {
        return  callGraph.getReachableClasses().contains(className);
    }
}
