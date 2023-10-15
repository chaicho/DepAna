package nju.lab.DScheckerMaven.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.analyze.BaseSmell;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Slf4j
public class MavenSharedLibrarySmell extends BaseSmell {
    MavenProject project;
    List<MavenProject> reactorProjects;
    Set<String> innerPaths = null;
    HashMap<String, MavenProject> depManagementBlockToProject = new HashMap<>();
    public MavenSharedLibrarySmell(MavenProject project, List<MavenProject> reactorProjects) {
        this.project = project;
        this.reactorProjects = reactorProjects;
    }

    public boolean versionFromCurrentProject(String versionLoc) {
        if (innerPaths == null) {
            innerPaths = new HashSet<>();
            for (MavenProject project : reactorProjects) {
                innerPaths.add(project.getFile().getPath());
            }
        }
        return innerPaths.contains(versionLoc);
    }
    public boolean versionFromCurrentProject(Dependency dep) {
        if (dep.getLocation("version") == null) {
//            If the dependency getLocation is null, it means its managed by the <scope>import</scope> in the dependency management block.
            log.warn("Artifact Not Found :", dep.getArtifactId());
            return true;
        }
        String versionLoc = dep.getLocation("version").getSource().toString();
        return versionFromCurrentProject(versionLoc);
    }
    public String getRelativeModulePath(MavenProject reactorProject, MavenProject rootProject) {
        try {
            String rootDir = rootProject.getFile().getParentFile().getCanonicalPath();
            String moduleDir = reactorProject.getFile().getParentFile().getCanonicalPath();
            if (moduleDir.length() < rootDir.length()) {
                return reactorProject.getFile().getPath();
            }
            else if (moduleDir.equals(rootDir)) {
                return ".";
            }
            return moduleDir.substring(rootDir.length());
        } catch (IOException e) {
            return reactorProject.getFile().getPath();
        }
    }
    public boolean isVersionSelfManaged(Dependency dep, MavenProject reactorProject) {
        if (dep.getLocation("version") == null) {
            log.warn("Artifact Not Found :", dep.getArtifactId());
            return true;
        }
        String versionLoc = dep.getLocation("version").getSource().toString();
        String artifactLoc = dep.getLocation("artifactId").getSource().toString();
//      The version is declared explicitly in the pom.xml, including dependencyManagment block or direct dependency.
        if (versionLoc == artifactLoc) {
            return true;
        }

//      The version is declared in the dependencyManagement block, but the version is from outside the project, so the artifactLoc is not current pom.xml
        if (depManagementBlockToProject.containsKey(dep.getManagementKey())) {
            return true;
        }

        return false;
    }
    @Override
    public void detect() {
        appendToResult("========SharedLibrarySmell========");
        Set<Dependency> allDependencies = new HashSet<>();
        HashMap<Dependency, Set<MavenProject>> unManagedDependencyToModule = new HashMap<>();
        HashMap<String,Set<Dependency>> managementKeytoDependency = new HashMap<>();
        HashMap<String, Set<MavenProject>> managedDependencyToModule = new HashMap<>();
        for (MavenProject reactorProject : reactorProjects) {

            List<Dependency> managedDeps = null;
            if (reactorProject.getDependencyManagement() != null) {
                managedDeps = reactorProject.getDependencyManagement().getDependencies();
                for (Dependency managedDep : managedDeps) {
                    if (!depManagementBlockToProject.containsKey(managedDep.getManagementKey())) {
                        depManagementBlockToProject.put(managedDep.getManagementKey(), reactorProject);
                    }
                }
            }
            // String moduleName = getRelativeModulePath(reactorProject, project);
            List<Dependency> dependencies = reactorProject.getDependencies();
            for (Dependency dependency : dependencies) {
                // If the dependency is not directly declared, skip it
                if (!dependency.getLocation("groupId").getSource().getLocation().equals(reactorProject.getFile().getPath())) {
                    continue;
                }
                if (!isVersionSelfManaged(dependency,reactorProject)) {
                    managedDependencyToModule.computeIfAbsent(dependency.getManagementKey(), k -> new HashSet<>()).add(reactorProject);
                    continue;
                }
                String managementKey = dependency.getManagementKey();
                managementKeytoDependency.computeIfAbsent(managementKey, k -> new HashSet<>()).add(dependency);
                unManagedDependencyToModule.computeIfAbsent(dependency, k -> new HashSet<>()).add(reactorProject);

            }
        }

        for (String managementKey : managementKeytoDependency.keySet()) {
            Set<Dependency> dependencies = managementKeytoDependency.get(managementKey);
            if ( (managedDependencyToModule.containsKey(managementKey) && !dependencies.isEmpty()) || dependencies.size() > 1) {
                appendToResult("Dependency " + managementKey + " is shared by multiple modules");
                for (Dependency dependency : dependencies) {
                    for (MavenProject module : unManagedDependencyToModule.get(dependency)) {
                        appendToResult("    " + getRelativeModulePath(module, project) + " : " + dependency.getGroupId()+ ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
                    }
                }
                if (managedDependencyToModule.containsKey(managementKey)) {
                    appendToResult("    " + "Some Are Managed by the dependencyManagement block");
                    for (MavenProject module : managedDependencyToModule.get(managementKey)) {
                        appendToResult("    " + getRelativeModulePath(module, project) + " : " + managementKey);
                    }
                }
                appendToResult("---------");
            }
        }
    }
}

