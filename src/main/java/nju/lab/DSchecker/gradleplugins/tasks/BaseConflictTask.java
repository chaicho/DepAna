package nju.lab.DSchecker.gradleplugins.tasks;

import lombok.Getter;
import neu.lab.conflict.container.AllCls;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.container.NodeAdapters;
import neu.lab.conflict.util.MyLogger;
import nju.lab.DSchecker.analyze.BloatedSmell;
import nju.lab.DSchecker.analyze.UnDeclaredSmell;
import nju.lab.DSchecker.model.HostProjectInfo;
import nju.lab.DSchecker.util.soot.TypeAna;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.*;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import neu.lab.conflict.util.GradleUtil;
import nju.lab.DSchecker.model.DepJar;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

@Getter
public abstract class BaseConflictTask extends DefaultTask {


    @Input
    public abstract ListProperty<ComponentArtifactIdentifier> getArtifactIdentifiers();

    @InputFiles
    public abstract ConfigurableFileCollection getArtifactFiles();

    @Input
    public abstract Property<ResolvedComponentResult> getRootComponent();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();


//    @InputFiles
//    public abstract ConfigurableFileCollection getSourceFiles();

//    @OutputDirectory
//    public abstract DirectoryProperty getOutputDir();


    private ArtifactCollection artifactCollection;
    private FileCollection fileCollection;
    private Set<ResolvedDependency> firstLevelModuleDependencies;
    private long systemFileSize;

    private long allJarNum;
    private int systemSize;

    public String target = "default";


    public Configuration configuration;

    public Project project;
    protected Map<ComponentIdentifier, Set<ResolvedArtifact> >  artifactMap;
    public String configurationName = "runtimeClasspath";
    private ResolvedConfiguration resolvedConfiguration;
    private Set<ResolvedArtifact> resolvedArtifacts;
    private Set<ResolvedArtifactResult> resolvedArtifactResults;
    private ResolutionResult resolutionResult;
    private ResolvedComponentResult root;
    private Set<? extends DependencyResult> rootDependencies;
    private ResolvableDependencies resolveableDependencies;

    public File buildDir;

    public Set<File> compileSrcDirs;

    public BaseConflictTask() {
        super();
    }

    private Map<ComponentIdentifier, File> filesByIdentifiers() {
        Map<ComponentIdentifier, File> map = new HashMap<>();
        List<ComponentArtifactIdentifier> identifiers = getArtifactIdentifiers().get();
        List<File> files = new ArrayList<>(getArtifactFiles().getFiles());
        for (int index = 0; index < identifiers.size(); index++) {
            map.put(identifiers.get(index).getComponentIdentifier(), files.get(index));
        }
        return map;
    }
    protected  Map<ComponentIdentifier, Set<ResolvedArtifact> >  initMapArtifactByIdentifiers() {
        Map<ComponentIdentifier, Set<ResolvedArtifact> >  map = new HashMap<>();
        for (ResolvedArtifact artifact : resolvedArtifacts) {
            ComponentIdentifier identifier = artifact.getId().getComponentIdentifier();
            if (!map.containsKey(identifier)) {
                map.put(identifier, new HashSet<>());
            }
            map.get(identifier).add(artifact);
        }
        return map;
    }

    private void validateSysSize() throws Exception {
        for (DepJar depJar : DepJars.i().getAllDepJar()) {
            if(!depJar.isSelected()){
//                continue;
            }
            systemSize++;
            for(String filePath: depJar.getJarFilePaths(true)) {
                System.out.println(filePath);
                systemFileSize = systemFileSize + new File(filePath).length();
            }

        }

        allJarNum = DepJars.i().getAllDepJar().size();
        MyLogger.i().warn("tree size:" + DepJars.i().getAllDepJar().size() + ", used size:" + systemSize
                + ", usedFile size:" + systemFileSize / 1000);
        //		if (DepJars.i().getAllDepJar().size() <= 50||systemFileSize / 1000>20000) {
        //			throw new Exception("project size error.");
        //		}
    }

    /*

        init global values
     */
    protected void initGlobalValues() {
        project = getProject();
        configuration = project.getConfigurations().getByName(configurationName);



        resolvedConfiguration = configuration.getResolvedConfiguration();
        resolvedArtifacts = resolvedConfiguration.getResolvedArtifacts();
        firstLevelModuleDependencies = resolvedConfiguration.getFirstLevelModuleDependencies();

        resolveableDependencies = configuration.getIncoming();
        resolutionResult = resolveableDependencies.getResolutionResult();
        root = resolutionResult.getRoot();
        rootDependencies = root.getDependencies();

        artifactCollection = resolveableDependencies.getArtifacts();
        fileCollection = artifactCollection.getArtifactFiles();
        resolvedArtifactResults = artifactCollection.getArtifacts();

        buildDir = project.getBuildDir();

        compileSrcDirs = project.getExtensions().getByType(SourceSetContainer.class)
                            .getByName("main").getAllJava().getSrcDirs();
//                                            .getByName("main").getAllJava().get()

        artifactMap = initMapArtifactByIdentifiers();


    }

    @TaskAction
    void execute() throws Exception {
        initGlobalValues();
        MyLogger.init(getLogger());
        GradleUtil.init(this);
        NodeAdapters.init(getRootComponent().get(),artifactMap);
        DepJars.init(NodeAdapters.i());
        validateSysSize();
        System.out.println("Calculate classes");

        AllCls.i().init(DepJars.i());
//        LibrarySmell.getInstance().detect();
//        ClassSmell.i().detect();
        HostProjectInfo.i().setCompileSrcFiles(compileSrcDirs);
        HostProjectInfo.i().setBuildDir(buildDir);
        HostProjectInfo.i().buildDepClassMap();


        TypeAna.i().analyze(DepJars.i().getUsedJarPaths());

        BloatedSmell.i().detect();
        UnDeclaredSmell.i().detect();




//        UnUsedSmell.i().detect();
//        System.out.println(DepJars.i().getUsedJarPaths());
//        System.out.println(buildDir.getAbsolutePath());
//        System.out.println("comilesrc"+ compileSrcDirs);





        File outputFile = getOutputFile().getAsFile().get();

        try ( PrintWriter writer = new PrintWriter(new FileWriter(outputFile))){
//            DepJars.i().getAllDepJar().forEach(depJar -> {
//                depJar.initClsTbRealTime();
//                depJar.getClsTb().forEach((clssig,classVO) -> {
//                    writer.println(classVO.getMthds());
//                });
//                }
//            );
//            projectInfo(writer);
//            writer.println(SootUtil.getInstance().getJarClasses("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\libs\\lib1\\build\\libs\\lib1.jar"));
//            writer.println(SootUtil.getInstance().getJarClasses("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\libs\\lib2\\build\\libs\\lib2.jar"));
//            writer.println(AllCls.i().getAllCls());
//            for (ResolvedDependency dependency : firstLevelModuleDependencies) {
//                dependency.getParents().forEach(parent -> {
//                    System.out.println(parent.getModuleGroup() + ":" + parent.getModuleName() + ":" + parent.getModuleVersion());
//                });
//                for (ResolvedArtifact artifact : dependency.getModuleArtifacts()) {
//                    if ("jar".equals(artifact.getType())) {
//                        String jarPath = artifact.getFile().getAbsolutePath();
//                        System.out.println("JAR import path: " + jarPath);
//                    }
//                }
//
//            }

            writer.println("=================ReportComponent===================");
            Set<ResolvedComponentResult> seen = new HashSet<>();
            reportComponent(root, writer, seen, "");

        }

//        Set<ResolvedComponentResult> seen = new HashSet<>();




    }
    private void projectInfo(PrintWriter writer){
        writer.println("========================================");
        firstLevelModuleDependencies.forEach(dep -> {
            writer.println(dep.getModuleGroup() + ":" + dep.getModuleName() + ":" + dep.getModuleVersion());
        });
        writer.println("========================================");
        writer.println("Project version: " + project.getVersion());
        writer.println("Project name: " + project.getName());
        writer.println("Project group: " + project.getGroup());
        writer.println("Project path: " + project.getPath());
        writer.println("Project description: " + project.getDescription());
        writer.println("Project buildDir: " + project.getBuildDir());
        writer.println("Project rootDir: " + project.getRootDir());
        writer.println("Project projectDir: " + project.getProjectDir());
        writer.println("Project gradleVersion: " + project.getGradle().getGradleVersion());
        writer.println("Project gradleHomeDir: " + project.getGradle().getGradleHomeDir());
        writer.println("Project gradleUserHomeDir: " + project.getGradle().getGradleUserHomeDir());
        writer.println("========================================");
        writer.println("Project Artifacts: " + configuration.getAllDependencies());
        writer.println("Project " + configurationName + " Artifacts: " + configuration.getDependencies());
        writer.println("========ResolvableDependencies=========");
        resolveableDependencies.getDependencies().forEach(dep -> {
            writer.println("Project " + configurationName + " Artifacts: " + dep.getGroup() + ":" + dep.getName() + ":" + dep.getVersion() +
                    "\nReason: " + dep.getReason());
        });
        writer.println("========ResolvedConfiguration=========");
        resolvedArtifacts.forEach(dep -> {
            writer.println("Project " + configurationName + " Artifacts: " + dep.getModuleVersion().getId().getGroup() + ":" + dep.getModuleVersion().getId().getName() + ":" + dep.getModuleVersion().getId().getVersion()
                    + "\n file:" + dep.getFile().getAbsolutePath() + "\n type: " + dep.getType() + "\n extension: " + dep.getExtension() + "\n classifier: " + dep.getClassifier() + "\n name: " + dep.getName());

        });
        writer.println("========ResolutionResult=========");
        resolutionResult.getAllComponents().forEach(dep -> {
            writer.println("Project " + configurationName + " Artifacts: " + dep.getModuleVersion().getGroup() + ":" + dep.getModuleVersion().getName() + ":" + dep.getModuleVersion().getVersion() );
            writer.println("Dependencies: " + dep.getDependencies());
            dep.getDependencies().forEach(dependencyResult -> {
                writer.println(dependencyResult.getFrom() + " ---> " + dependencyResult.getRequested());
//                    writer.println(dependencyResult.getRequested());
            });
            writer.println("Dependents: " + dep.getDependents());
            writer.println("Variants : " + dep.getVariants());
            writer.println("Id : " + dep.getId());
            writer.println();
        });

        writer.println("===================Root=================");
        writer.println("Root: " + root.getModuleVersion().getGroup() + ":" + root.getModuleVersion().getName() + ":" + root.getModuleVersion().getVersion() );
        writer.println("Dependencies: " + root.getDependencies());
        writer.println("Dependents: " + root.getDependents());
        writer.println("Variants : " + root.getVariants());
        writer.println("Id : " + root.getId());
        root.getDependencies().forEach(dependencyResult -> {
            ResolvedDependencyResult resolvedDependencyResult = (ResolvedDependencyResult) dependencyResult;
            ResolvedComponentResult resolvedComponentResult = resolvedDependencyResult.getSelected();
            writer.println(dependencyResult.getFrom() + " ---> " + dependencyResult.getRequested());
            writer.println(resolvedDependencyResult.getSelected().getModuleVersion());
            writer.println(resolvedComponentResult.getDependencies());

        });
        root.getVariants().forEach(variant -> {
                    writer.println("Variant: " + variant.getDisplayName());
                    writer.println("Attributes: " + variant.getAttributes());
                    writer.println("Owner" + variant.getOwner());
                }
        );

    }
    private void reportComponent(
            ResolvedComponentResult component,
            PrintWriter writer,
            Set<ResolvedComponentResult> seen,
            String indent
    ) {
//        writer.println(component.getId());

        if (seen.add(component)) {
            writer.println();
            String newIndent = indent + "  ";
            for (DependencyResult dependency : component.getDependencies()) {
                writer.print(newIndent);
                writer.print(dependency.getRequested().getDisplayName());
                writer.print(" -> ");
                if (dependency instanceof ResolvedDependencyResult) {
                    ResolvedDependencyResult resolvedDependency = (ResolvedDependencyResult) dependency;
                    reportComponent(resolvedDependency.getSelected(), writer, seen, newIndent);
                    resolvedDependency.getSelected().getVariants().forEach(variant ->{
                    });
                } else {
                    writer.println(" -> not found");
                }
            }
        } else {
            writer.println(" (already seen)");
        }
    }

}
