package nju.lab.DScheckerMaven.mojos;


import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.SmellFactory;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.util.soot.TypeAna;
import nju.lab.DScheckerMaven.model.*;
import nju.lab.DScheckerMaven.util.Conf;
import nju.lab.DScheckerMaven.util.MavenUtil;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
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

@Slf4j
@Mojo(name = "testDSchecker",defaultPhase = LifecyclePhase.COMPILE)
public class BaseMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    public MavenProject project;
    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    public List<MavenProject> reactorProjects;
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    public List<ArtifactRepository> remoteRepositories;
    @Parameter(defaultValue = "${localRepository}", readonly = true)
    public ArtifactRepository localRepository;

    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
    public List<String> compileSourceRoots;
    @Component
    public ArtifactFactory factory;
    @Component
    public ArtifactHandlerManager artifactHandlerManager;
    @Component
    public ArtifactResolver resolver;
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    public File buildDir;
    @Component
    public DependencyTreeBuilder dependencyTreeBuilder;

    public DependencyNode root;

    public boolean ignoreProvidedScope = true;
    public boolean ignoreTestScope = true;
    public boolean ignoreTestClassifier = true;
    public boolean ignoreRuntimeScope = true;

    protected void initGlobalValues() {
        /**
         * 配置一系列重要参数：
         */
        MavenUtil.getInstance().setMojo(this); //在MavenUtil设置一下插件指向自己

    }
    /**
     * 初始化全局变量
     * @throws Exception
     */
    protected void initGlobalVar() throws Exception {
        initGlobalValues();
        //初始化NodeAdapters
        NodeAdapters.init(root);
        long startTime = System.currentTimeMillis();//
        /**
         * 对全部被使用的(isNodeSelected依赖节点，找到他们的pom路径，然后检查他们是不是exclude了一些依赖，
         * 把这些被exclude掉的依赖放进Conf.i().dependencyMap(通过对每个依赖的pom都调用detectExclude(localPomPath, node))
         */
        //初始化DepJars
        DepJars.init(NodeAdapters.i());// occur jar in tree
        validateSysSize();
        HostProjectInfo.i().init(CallGraphMaven.i(), DepJars.i());
        HostProjectInfo.i().setBuildDir(buildDir);
        HostProjectInfo.i().setCompileSrcPaths(compileSourceRoots);

        //初始化所有的类集合
//        log.info("寻找有多版本的artifact...");

        //Artifacts.init(NodeAdapters.i());// all depJars, with all versions occured
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
        IDepJar depJar = null;
        System.out.println("Hello World4!");
//        System.out.println(project);
//        System.out.println(localRepository);
        System.out.println(compileSourceRoots);
        System.out.println(buildDir);
        String pckType = project.getPackaging();	//得到项目的打包类型
        if ("jar".equals(pckType) || "war".equals(pckType) || "maven-plugin".equals(pckType)
                || "bundle".equals(pckType)) {
            try {
                // project.
                root = dependencyTreeBuilder.buildDependencyTree(project, localRepository, null);
                //graphNode = dependencyGraphBuilder.buildDependencyGraph(project, null);
            } catch (DependencyTreeBuilderException /*| DependencyGraphBuilderException*/ e) {
                throw new MojoExecutionException(e.getMessage());
            }
            /**
             * 接下来，初始化全局变量
             */
            try {
                initGlobalVar();
            } catch (Exception e) { System.err.println("Caught Exception!");
                MavenUtil.getInstance().getLog().error(e);
                throw new MojoExecutionException("project size error!");
            }
        }

        try {
            initGlobalVar();
        } catch (Exception e) {
            System.out.println("initGlobalVar error");
            throw new RuntimeException(e);
        }
        System.out.println("---UsedJarPaths---");
        System.out.println(DepJars.i().getUsedJarPaths());
        System.out.println("----analyze----");

        TypeAna.i().setHostProjectInfo(HostProjectInfo.i());
        TypeAna.i().analyze(DepJars.i().getUsedJarPaths());

        System.out.println("----buildDepClassMap----");

        HostProjectInfo.i().init(CallGraphMaven.i(), DepJars.i());
        HostProjectInfo.i().buildDepClassMap();


        SmellFactory smellFactory = new SmellFactory();
        smellFactory.init(HostProjectInfo.i(), DepJars.i(), CallGraphMaven.i());
        smellFactory.detectAll();
    }
}