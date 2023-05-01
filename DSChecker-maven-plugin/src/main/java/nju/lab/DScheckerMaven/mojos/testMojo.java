package nju.lab.DScheckerMaven.mojos;


import nju.lab.DSchecker.core.model.IDepJar;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "testDSchecker",defaultPhase = LifecyclePhase.COMPILE)
public class testMojo extends AbstractMojo {
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

    @Override
    public void execute() {
        IDepJar depJar = null;
        System.out.println("Hello World1!");
        System.out.println(project);
        System.out.println(reactorProjects);
        System.out.println(remoteRepositories);
        System.out.println(localRepository);
        System.out.println(compileSourceRoots);
        System.out.println(artifactHandlerManager);

    }
}