package nju.lab.DSchecker.gradleplugins;//import neu.lab.conflict.tasks.DownloadArtifactTask;
import nju.lab.DSchecker.gradleplugins.tasks.GraphResolvedComponentsAndFiles;
import nju.lab.DSchecker.gradleplugins.tasks.LibrarySmellDetectTask;
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
import org.gradle.api.tasks.TaskContainer;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class GradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPluginManager().withPlugin("java-base", plugin -> {

            ProjectLayout layout = project.getLayout();
            ConfigurationContainer configurations = project.getConfigurations();
            TaskContainer tasks = project.getTasks();


            tasks.register("graphResolvedComponentsAndFiles", GraphResolvedComponentsAndFiles.class, task -> {

                ResolvableDependencies resolvableDependencies = configurations.getByName("compileClasspath").getIncoming();
                Provider<Set<ResolvedArtifactResult>> resolvedArtifacts = resolvableDependencies.getArtifacts().getResolvedArtifacts();
                task.getArtifactFiles().from(resolvableDependencies.getArtifacts().getArtifactFiles());
                task.getArtifactIdentifiers().set(resolvedArtifacts.map(result -> result.stream().map(ResolvedArtifactResult::getId).collect(toList())));
                task.getRootComponent().set(resolvableDependencies.getResolutionResult().getRootComponent());

                task.getOutputFile().set(layout.getBuildDirectory().file(task.getName() + "/report.txt"));
            });
            tasks.register("LibrarySmell", LibrarySmellDetectTask.class, task -> {
//                task.setGroup("org.apache.commons");
//                task.setName("commons-collections4");
//                task.setName("4.2");
//                task.setDescription("Download artifact from a repository.");
            });
//            tasks.register("DownloadArtifactTask", DownloadArtifactTask.class, task -> {
//                task.setGroup("org.apache.commons");
//                task.setName("commons-collections4");
//                task.setName("4.2");
//                task.setDescription("Download artifact from a repository.");
//            });
        });
    }

    static class IdExtractor
            implements Transformer<List<ComponentArtifactIdentifier>, Collection<ResolvedArtifactResult>> {
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
