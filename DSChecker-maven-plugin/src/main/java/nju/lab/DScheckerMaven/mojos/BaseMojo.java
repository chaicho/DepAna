package nju.lab.DScheckerMaven.mojos;


import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DScheckerMaven.util.MavenUtil;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolver;

import java.io.File;
import java.util.List;

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
    public boolean ignoreProvidedScope = true;
    public boolean ignoreTestScope = true;
    public boolean ignoreTestClassifier = true;
    public boolean ignoreRuntimeScope = true;

    @Override
    public void execute() {
        IDepJar depJar = null;
        System.out.println("Hello World2!");
        System.out.println(project);
        System.out.println(reactorProjects);
        System.out.println(remoteRepositories);
        System.out.println(localRepository);
        System.out.println(compileSourceRoots);
        System.out.println(artifactHandlerManager);

    }
}