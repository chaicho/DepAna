package nju.lab.DScheckerMaven.mojos;


import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.SmellFactory;
import nju.lab.DSchecker.core.analyze.WrapperConfMissingSmell;
import nju.lab.DSchecker.core.analyze.WrapperJarAbnormalSmell;
import nju.lab.DSchecker.core.analyze.WrapperJarMissingSmell;
import nju.lab.DSchecker.core.model.DepModel;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.monitor.PerformanceMonitor;
import nju.lab.DSchecker.util.soot.TypeAna;
import nju.lab.DSchecker.util.source.analyze.FullClassExtractor;
import nju.lab.DScheckerMaven.core.analyze.MavenConflictLibrarySmell;
import nju.lab.DScheckerMaven.core.analyze.MavenSharedLibrarySmell;
import nju.lab.DScheckerMaven.model.*;
import nju.lab.DScheckerMaven.core.analyze.MavenLibraryScopeMisuseSmell;
import nju.lab.DScheckerMaven.util.Conf;
import nju.lab.DScheckerMaven.util.MavenUtil;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@Mojo(name = "project",defaultPhase = LifecyclePhase.COMPILE)
public class ProjectLevelSmellMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    public MavenProject project;
    @Parameter(defaultValue = "${reactorProjects}", readonly = true)
    public List<MavenProject> reactorProjects;
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    public List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Parameter(defaultValue = "${localRepository}", readonly = true)
    public ArtifactRepository localRepository;

    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
    public List<String> compileSourceRoots;

    @Parameter(defaultValue = "${project.testCompileSourceRoots}", readonly = true, required = true)
    public List<String> testCompileSourceRoots;
    @Component
    public ArtifactFactory factory;
    @Component
    public ArtifactHandlerManager artifactHandlerManager;
    @Component
    public ArtifactResolver resolver;
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    public File buildDir;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    public File outputDir;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}", required = true)
    public File testOutputDir;

    @Component
    public DependencyTreeBuilder dependencyTreeBuilder;

    public DependencyNode root;

    public boolean ignoreProvidedScope = false;
    public boolean ignoreTestScope = false;
    public boolean ignoreTestClassifier = false;
    public boolean ignoreRuntimeScope = false;

    protected void initGlobalValues() {
        /**
         * 配置一系列重要参数：
         */
//        MavenUtil.getInstance().setMojo(this); //在MavenUtil设置一下插件指向自己
    }
    /**
     * 初始化全局变量
     * @throws Exception
     */
    protected void initGlobalVar() throws Exception {
        initGlobalValues();
//        PerformanceMonitor.initialize(buildDir.getAbsolutePath() + File.separator + "supportData.xml");
//        validateSysSize();
        // 项目信息处理
//        PerformanceMonitor.start("initHostProjectInfo");
        HostProjectInfo.i().setResultFileName("DScheckerResultProjectLevel.txt");
        HostProjectInfo.i().setBuildDir(buildDir);
        HostProjectInfo.i().setOutputDir(outputDir);
        HostProjectInfo.i().setTestOutputDir(testOutputDir);
        HostProjectInfo.i().setCompileSrcPaths(compileSourceRoots);
        HostProjectInfo.i().setTestCompileSrcPaths(testCompileSourceRoots);
        HostProjectInfo.i().setRootDir(new File(mavenSession.getExecutionRootDirectory()));

        // Dependency Model Build
        DepModel depModel = new DepModel(CallGraphMaven.i(), DepJars.i(), HostProjectInfo.i());




    }

    private void validateSysSize() throws Exception {
        long systemSize = 0;
        long systemFileSize = 0;
        for (DepJar depJar : DepJars.i().getAllDepJar()) {
            if (depJar.isSelected()) {
                systemSize++;
                for (String filePath : depJar.getJarFilePaths(true)) {
                    systemFileSize = systemFileSize + new File(filePath).length();
                }
            }
        }
        int allJarNum = DepJars.i().getAllDepJar().size();
        log.info("tree size:" + DepJars.i().getAllDepJar().size() + ", used size:" + systemSize
                + ", usedFile size:" + systemFileSize / 1000);
    }
    @Override
    public void execute() throws MojoExecutionException {
        if (!project.equals(mavenSession.getTopLevelProject())) {
            System.out.println("not top level project");
            return;
        }
        // if (project.getPackaging() == null && !project.getPackaging().equals("pom")) {
        //     return;
        // }
        // if (project.getParent() != null) {
        //     System.out.println(project.getParent().getArtifactId());
        //     return;
        // }
        
        System.out.println("project level smell");
        try {
            initGlobalVar();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SmellFactory smellFactory = new SmellFactory();
        smellFactory.initOnly(HostProjectInfo.i(), DepJars.i(), CallGraphMaven.i());
        MavenSharedLibrarySmell mavenSharedLibrarySmell = new MavenSharedLibrarySmell(project,reactorProjects);
        MavenConflictLibrarySmell mavenConflictLibrarySmell = new MavenConflictLibrarySmell(project,reactorProjects);
        smellFactory.addSmell(new WrapperJarMissingSmell());
        smellFactory.addSmell(new WrapperConfMissingSmell());
        smellFactory.addSmell(new WrapperJarAbnormalSmell());
        smellFactory.addSmell(mavenSharedLibrarySmell);
        smellFactory.addSmell(mavenConflictLibrarySmell);
        smellFactory.detectAll();
    }
}