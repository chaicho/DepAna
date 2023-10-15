package nju.lab.DScheckerMaven.core.analyze;

import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DScheckerMaven.model.DepJar;
import nju.lab.DScheckerMaven.model.GroupArtifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenSharedLibrarySmell extends BaseSmell {
    MavenProject project;
    List<MavenProject> reactorProjects;
    public MavenSharedLibrarySmell(MavenProject project, List<MavenProject> reactorProjects) {
        this.project = project;
        this.reactorProjects = reactorProjects;
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
    public boolean isVersionSelfDeclared(Dependency dep) {
//         return dep.versionLocation.equals(curPom);
        String versionLoc = dep.getLocation("version").getSource().toString();
        String artifactLoc = dep.getLocation("artifactId").getSource().toString();
        return versionLoc.equals(artifactLoc);
    }
    public boolean isVersionSelfDeclared(Dependency dep, List<Dependency> managedDeps) {
        if (dep.getLocation("version") == null) {
//            If the dependency getLocation is null, it means its managed by the <scope>import</scope> in the dependency management block.
            return true;
        }
        String versionLoc = dep.getLocation("version").getSource().toString();
        String artifactLoc = dep.getLocation("artifactId").getSource().toString();
        // If is declared in dependencyManagement, then it is self declared only if the version is different.
        if (managedDeps != null) {
            for (Dependency managedDep : managedDeps) {
                if (managedDep.getManagementKey().equals(dep.getManagementKey())) {
                    if (!managedDep.getVersion().equals(dep.getVersion())) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return versionLoc.equals(artifactLoc);
    }
    @Override
    public void detect() {
        appendToResult("========SharedLibrarySmell========");
        Set<Dependency> allDependencies = new HashSet<>();
        Set<String> managedDependencies = new HashSet<>();
        HashMap<Dependency, Set<MavenProject>> unManagedDependencyToModule = new HashMap<>();
        HashMap<String,Set<Dependency>> managementKeytoDependency = new HashMap<>();
        HashMap<String, Set<MavenProject>> managedDependencyToModule = new HashMap<>();
        for (MavenProject reactorProject : reactorProjects) {

            List<Dependency> managedDeps = null;
            if (reactorProject.getDependencyManagement() != null) {
                managedDeps = reactorProject.getDependencyManagement().getDependencies();
                for (Dependency managedDep : managedDeps) {
                    managedDependencies.add(managedDep.getManagementKey());
                }
            }
            // String moduleName = getRelativeModulePath(reactorProject, project);
            List<Dependency> dependencies = reactorProject.getDependencies();
            for (Dependency dependency : dependencies) {
                // If the dependency is not directly declared, skip it
                if (!dependency.getLocation("groupId").getSource().getLocation().equals(reactorProject.getFile().getPath())) {
                    continue;
                }
                if (!isVersionSelfDeclared(dependency,managedDeps)) {
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
            if ( (managedDependencies.contains(managementKey) && !dependencies.isEmpty()) || dependencies.size() > 1) {
                appendToResult("Dependency " + managementKey + " is shared by multiple modules");
                for (Dependency dependency : dependencies) {
                    for (MavenProject module : unManagedDependencyToModule.get(dependency)) {
                        appendToResult("    " + getRelativeModulePath(module, project) + " : " + dependency.getGroupId()+ ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
                    }
                }
                if (managedDependencies.contains(managementKey)) {
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

