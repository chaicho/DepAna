package nju.lab.DScheckerGradle.model;

import nju.lab.DSchecker.core.model.*;
import org.gradle.api.file.FileCollection;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import soot.SourceLocator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class HostProjectInfo extends IHostProjectInfo {
    private static HostProjectInfo instance;
    private Set<String> apiDepJars = new HashSet<>();
    private FileCollection classesDirs;
    private String buildTestCp;
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
    public void setBuildTestCp(String buildTestCp) {
        this.buildTestCp = buildTestCp;
    }
    @Override
    public String getBuildTestCp() {
        return buildTestCp;
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
    @Override
    public IDepJar getFirstUsedDepFromClassWithTargetScene(String className, String scene) {
        if(usedDependenciesPerClass.get(className).size() == 0)
            return null;
        Set<String> appropriateScopes = new HashSet<String>();
        if (scene == "compile") {
            appropriateScopes.add("compileOnly");
            appropriateScopes.add("implementation");
            appropriateScopes.add("api");
            appropriateScopes.add("compileOnlyApi");
        }
        else if (scene == "runtime") {
            appropriateScopes.add("runtimeOnly");
            appropriateScopes.add("implementation");
            appropriateScopes.add("api");
        }
        else if (scene == "test") {
            appropriateScopes.add("compileOnlyApi");
            appropriateScopes.add("testCompileOnly");
            appropriateScopes.add("implementation");
            appropriateScopes.add("testImplementation");
            appropriateScopes.add("runtimeOnly");
            appropriateScopes.add("testRuntimeOnly");
        }
        for (IDepJar depJar : usedDependenciesPerClass.get(className)) {
            if(appropriateScopes.contains(depJar.getScope()))
                return depJar;
        }
        // When reach here, it means that there is no class at given scene, so it means that there should be a scope problem
        // We return the first one, though not in the scope.
        IDepJar depJar = usedDependenciesPerClass.get(className).iterator().next();
        log.warn("Class " + className + " is not in the scene " + scene + " but in " + depJar.getScope());
        return depJar;
    }
    @Override
    public String getBuildTool() {
        return "gradle";
    }
}
