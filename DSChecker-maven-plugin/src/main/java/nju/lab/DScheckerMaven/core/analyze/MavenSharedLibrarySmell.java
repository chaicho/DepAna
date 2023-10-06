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
            return moduleDir.substring(rootDir.length() + 1);
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
        HashMap<Dependency, Set<MavenProject>> dependencyToModule = new HashMap<>();
        HashMap<String,Set<Dependency>> ManagementKeytoDependency = new HashMap<>();

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
                if (!isVersionSelfDeclared(dependency,managedDeps)) {
                    continue;
                }
                String managementKey = dependency.getManagementKey();
                if (!ManagementKeytoDependency.containsKey(managementKey)) {
                    ManagementKeytoDependency.put(managementKey, new HashSet<>());
                }
                if (!dependencyToModule.containsKey(dependency)) {
                    dependencyToModule.put(dependency, new HashSet<>());
                }
                ManagementKeytoDependency.get(managementKey).add(dependency);
                dependencyToModule.get(dependency).add(reactorProject);
            }
        }
        for (String managementKey : ManagementKeytoDependency.keySet()) {
            Set<Dependency> dependencies = ManagementKeytoDependency.get(managementKey);
            if (managedDependencies.contains(managementKey)) {
//                Have dependencyManagement block, but some dependencies are not controlled.
                appendToResult("Dependency " + managementKey + " is shared by multiple modules, part of them are not controlled by dependencyManagement.");
                for (Dependency dependency : dependencies) {
                        for (MavenProject module : dependencyToModule.get(dependency)) {
                            appendToResult("    " + getRelativeModulePath(module, project) + " : " + dependency.getGroupId()+ ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
                        }
                }
                appendToResult("---------");
            }
            if (dependencies.size() > 1) {
                appendToResult("Dependency " + managementKey + " is shared by multiple modules");
                for (Dependency dependency : dependencies) {
                    for (MavenProject module : dependencyToModule.get(dependency)) {
                        appendToResult("    " + getRelativeModulePath(module, project) + " : " + dependency.getGroupId()+ ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
                    }
                }
                appendToResult("---------");
            }
        }
    }
}

