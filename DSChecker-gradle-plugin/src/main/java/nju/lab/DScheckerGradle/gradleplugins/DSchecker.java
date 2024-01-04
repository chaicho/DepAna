package nju.lab.DScheckerGradle.gradleplugins;

import nju.lab.DScheckerGradle.gradleplugins.tasks.ProjectLevelSmellTask;
import nju.lab.DScheckerGradle.gradleplugins.tasks.ReportArtifactMetadataTask;
import nju.lab.DScheckerGradle.gradleplugins.tasks.ReportDependencyGraphTask;
import nju.lab.DScheckerGradle.gradleplugins.tasks.BaseConflictTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import org.gradle.api.Transformer;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedVariantResult;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class DSchecker implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-base", plugin -> {
            ProjectLayout layout = project.getLayout();
            ConfigurationContainer configurations = project.getConfigurations();
            TaskContainer tasks = project.getTasks();
            tasks.register("DScheck", BaseConflictTask.class, task->{
                try {
                    TaskProvider<JavaCompile> compileJavaTask = project.getTasks().named("compileJava", JavaCompile.class);
                    task.dependsOn(compileJavaTask);
                } catch (UnknownTaskException e) {
                    System.out.println("Task with name 'compileJava' not found in project " + project.getName());
                }
                try {
                    TaskProvider<JavaCompile> compileGeneratedJavaTask = project.getTasks().named("compileGeneratedJava", JavaCompile.class);
                    task.dependsOn(compileGeneratedJavaTask);
                } catch (UnknownTaskException e) {
                    System.out.println("Task with name 'compileGeneratedJava' not found in project " + project.getName());
                }
            });
        });
        TaskContainer tasks = project.getTasks();
        tasks.register("DScheckProject", ProjectLevelSmellTask.class, task -> {
                    return;
        });
//         project.getTasks().register("artifacts-report", ReportArtifactMetadataTask.class, t -> {
//             Provider<Set<ResolvedArtifactResult>> artifacts = project.getConfigurations().getByName("runtimeClasspath").getIncoming().getArtifacts().getResolvedArtifacts();
//             t.getArtifactIds().set(artifacts.map(new IdExtractor()));
//             t.getArtifactVariants().set(artifacts.map(new VariantExtractor()));
//             t.getArtifactFiles().set(artifacts.map(new FileExtractor(project.getLayout())));
//             t.getOutputFile().set(project.getLayout().getBuildDirectory().file("artifacts.txt"));
//         });

// //
//         project.getTasks().register("graph-report", ReportDependencyGraphTask.class, t -> {
//             Provider<ResolvedComponentResult> rootComponent = project.getConfigurations().getByName("runtimeClasspath").getIncoming().getResolutionResult().getRootComponent();
//             t.getRootComponent().set(rootComponent);
//             t.getOutputFile().set(project.getLayout().getBuildDirectory().file("graph.txt"));
//         });
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
