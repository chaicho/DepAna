package nju.lab.DScheckerGradle.model;

import nju.lab.DSchecker.core.model.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import soot.SourceLocator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HostProjectInfo extends IHostProjectInfo {
    private static HostProjectInfo instance;
    private Set<String> apiDepJars = new HashSet<>();
    private FileCollection classesDirs;
    private String buildTestCp;
    private Project project;
    private HashMap<String, Set<String> > appropriateScopesMap;
    private HostProjectInfo() {
    }
    public static void init() {
        instance = null;
    }
    public static void reset() {
        instance =  null;
    }
    public static HostProjectInfo i() {
        if (instance == null) {
            instance = new HostProjectInfo();
        }
        return instance;
    }
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public String getName() {
        System.out.println("project.getName(): " + project.getName());
        System.out.println(project.getName());
        return project.getName();
    }

    public Set<Configuration> getAllExtendsFromConf(Configuration targetConf) {
        Set<Configuration> extendsFrom = new HashSet<>();
        if (!targetConf.isCanBeConsumed() && !targetConf.isCanBeResolved() ) {
            extendsFrom.add(targetConf);
        }
        for (Configuration conf : targetConf.getExtendsFrom()) {
            extendsFrom.addAll(getAllExtendsFromConf(conf));
        }
        return extendsFrom;
    }

    public void initAppropriateScopes(Project project) {
        Set<String> compileConfNames = new HashSet<>();
        Set<Configuration> compileConfs = getAllExtendsFromConf(project.getConfigurations().getByName("compileClasspath"));
        for (Configuration conf : compileConfs) {
            compileConfNames.add(conf.getName());
        }
        Set<String> runtimeConfNames = new HashSet<>();
        Set<Configuration> runtimeConfs = getAllExtendsFromConf(project.getConfigurations().getByName("runtimeClasspath"));
        for (Configuration conf : runtimeConfs) {
            runtimeConfNames.add(conf.getName());
        }
        Set<String> testConfNames = new HashSet<>();
        Set<Configuration> testCompileConfs = getAllExtendsFromConf(project.getConfigurations().getByName("testCompileClasspath"));
        Set<Configuration> testRuntimeConfs = getAllExtendsFromConf(project.getConfigurations().getByName("testRuntimeClasspath"));
        for (Configuration conf : testCompileConfs) {
            testConfNames.add(conf.getName());
        }
        for (Configuration conf : testRuntimeConfs) {
            testConfNames.add(conf.getName());
        }
        appropriateScopesMap = new HashMap<>();
        appropriateScopesMap.put("compile", compileConfNames);
        appropriateScopesMap.put("runtime", runtimeConfNames);
        appropriateScopesMap.put("test", testConfNames);
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
        Set<String> appropriateScopes = appropriateScopesMap.get(scene);
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
