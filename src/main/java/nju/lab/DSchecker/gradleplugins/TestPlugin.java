package nju.lab.DSchecker.gradleplugins;

import nju.lab.DSchecker.gradleplugins.tasks.ReportArtifactMetadataTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedVariantResult;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import nju.lab.DSchecker.gradleplugins.tasks.BaseConflictTask;
import org.gradle.api.tasks.TaskContainer;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class TestPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-base", plugin -> {
            ProjectLayout layout = project.getLayout();
            ConfigurationContainer configurations = project.getConfigurations();
            TaskContainer tasks = project.getTasks();
            tasks.register("testBase", BaseConflictTask.class, task->{
                ResolvableDependencies resolvableDependencies = configurations.getByName("runtimeClasspath").getIncoming();
                Provider<Set<ResolvedArtifactResult>> resolvedArtifacts = resolvableDependencies.getArtifacts().getResolvedArtifacts();

                task.getArtifactFiles().from(resolvableDependencies.getArtifacts().getArtifactFiles());
                task.getArtifactIdentifiers().set(resolvedArtifacts.map(result -> result.stream().map(ResolvedArtifactResult::getId).collect(toList())));
                task.getRootComponent().set(resolvableDependencies.getResolutionResult().getRootComponent());
                task.getOutputFile().set(project.getLayout().getBuildDirectory().file("test.txt"));
//                task.getSourceFiles().setFrom(project.getExtensions().getByType(SourceSetContainer.class)
//                                            .getByName("main").getAllJava().get());
            });
        });

        project.getPlugins().apply("java-library");

        project.getTasks().register("artifacts-report", ReportArtifactMetadataTask.class, t -> {
            Provider<Set<ResolvedArtifactResult>> artifacts = project.getConfigurations().getByName("runtimeClasspath").getIncoming().getArtifacts().getResolvedArtifacts();
            t.getArtifactIds().set(artifacts.map(new IdExtractor()));
            t.getArtifactVariants().set(artifacts.map(new VariantExtractor()));
            t.getArtifactFiles().set(artifacts.map(new FileExtractor(project.getLayout())));
            t.getOutputFile().set(project.getLayout().getBuildDirectory().file("artifacts.txt"));
        });

//
//        target.getTasks().register("graph-report", neu.lab.tasks.ReportDependencyGraphTask.class, t -> {
//            Provider<ResolvedComponentResult> rootComponent = target.getConfigurations().getByName("runtimeClasspath").getIncoming().getResolutionResult().getRootComponent();
//            t.getRootComponent().set(rootComponent);
//            t.getOutputFile().set(target.getLayout().getBuildDirectory().file("graph.txt"));
//        });
    }

    static class IdExtractor implements Transformer<List<ComponentArtifactIdentifier>, Collection<ResolvedArtifactResult>> {
        @Override
        public List<ComponentArtifactIdentifier> transform(Collection<ResolvedArtifactResult> artifacts) {
            return artifacts.stream().map(ResolvedArtifactResult::getId).collect(Collectors.toList());
        }
    }

    static class VariantExtractor implements Transformer<List<ResolvedVariantResult>, Collection<ResolvedArtifactResult>> {
        @Override
        public List<ResolvedVariantResult> transform(Collection<ResolvedArtifactResult> artifacts) {
            return artifacts.stream().map(ResolvedArtifactResult::getVariant).collect(Collectors.toList());
        }
    }

    static class FileExtractor implements Transformer<List<RegularFile>, Collection<ResolvedArtifactResult>> {
        private final ProjectLayout projectLayout;

        public FileExtractor(ProjectLayout projectLayout) {
            this.projectLayout = projectLayout;
        }

        @Override
        public List<RegularFile> transform(Collection<ResolvedArtifactResult> artifacts) {
            Directory projectDirectory = projectLayout.getProjectDirectory();
            return artifacts.stream().map(a -> projectDirectory.file(a.getFile().getAbsolutePath())).collect(Collectors.toList());
        }
    }
}
