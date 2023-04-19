//package neu.lab.conflict.tasks;
//
//import org.gradle.api.DefaultTask;
//import org.gradle.api.artifacts.*;
//import org.gradle.api.artifacts.component.ComponentIdentifier;
//import org.gradle.api.artifacts.dsl.RepositoryHandler;
//import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
//import org.gradle.api.artifacts.result.ResolutionResult;
//import org.gradle.api.artifacts.result.ResolvedArtifactResult;
//import org.gradle.api.logging.Logger;
//import org.gradle.api.logging.Logging;
//import org.gradle.api.tasks.Input;
//import org.gradle.api.tasks.TaskAction;
//
//import  java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public class DownloadArtifactTask extends DefaultTask {
//
//    private static final Logger LOGGER = Logging.getLogger(DownloadArtifactTask.class);
//
//    @Input
//    private String group;
//
//    @Input
//    private String name;
//
//    @Input
//    private String version;
//
//    @Input
//    private String repositoryUrl;
//
//    public void setGroup(String group) {
//        this.group = group;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }
//
//    public void setRepositoryUrl(String repositoryUrl) {
//        this.repositoryUrl = repositoryUrl;
//    }
//
//    @TaskAction
//    public void downloadArtifact() {
//        if (group == null || name == null || version == null || repositoryUrl == null) {
//            throw new IllegalArgumentException("Group, name, version and repository URL must be specified.");
//        }
//
//        RepositoryHandler repositoryHandler = getProject().getRepositories();
//        MavenArtifactRepository repository = repositoryHandler.maven(repo -> {
//            repo.setUrl(repositoryUrl);
//        });
//
//        Configuration configuration = getProject().getConfigurations().detachedConfiguration(
//                getProject().getDependencies().create(group + ":" + name + ":" + version));
//
//        configuration.setTransitive(false);
//        configuration.getResolutionStrategy().cacheChangingModulesFor(0, "seconds");
//
//        ResolutionResult resolutionResult = configuration.getIncoming().getResolutionResult();
//        List<ComponentIdentifier> componentIds = configuration.getAllDependencies().stream()
//                .filter(dependency -> dependency instanceof ExternalModuleDependency)
//                .map(dependency -> ((ExternalModuleDependency) dependency).getArtifacts())
//                .map(artifacts -> artifacts.iterator().next())
//                .filter(dependencyArtifact -> dependencyArtifact instanceof ResolvedArtifactResult)
//                .map(dependencyArtifact -> ((ResolvedArtifactResult) dependencyArtifact).getId().getComponentIdentifier())
//                .collect(Collectors.toList());
//
//    }
//}
