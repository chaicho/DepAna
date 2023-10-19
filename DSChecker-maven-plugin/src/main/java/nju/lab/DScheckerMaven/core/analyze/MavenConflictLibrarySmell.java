package nju.lab.DScheckerMaven.core.analyze;

import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.ICallGraph;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.core.model.IDepJars;
import nju.lab.DSchecker.core.model.IHostProjectInfo;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenConflictLibrarySmell extends BaseSmell {
    MavenProject rootProject;
    List<MavenProject> reactorProjects;

    HashMap<String, Set<String>> depManagementKeyToDep = new HashMap<>();
    HashMap<String, Set<MavenProject>> depToModules = new HashMap<>();
    public String getRelativeModulePath(MavenProject reactorProject, MavenProject rootProject) {
        try {
            String rootDir = rootProject.getFile().getParentFile().getCanonicalPath();
            String moduleDir = reactorProject.getFile().getParentFile().getCanonicalPath();
            if (moduleDir.length() < rootDir.length()) {
                return reactorProject.getFile().getPath();
            } else if (moduleDir.equals(rootDir)) {
                return ".";
            }
            return moduleDir.substring(rootDir.length());
        } catch (IOException e) {
            return reactorProject.getFile().getPath();
        }
    }

    public MavenConflictLibrarySmell(MavenProject project, List<MavenProject> reactorProjects) {
        this.rootProject = project;
        this.reactorProjects = reactorProjects;
    }

    @Override
    public void detect() {
        for (MavenProject project : reactorProjects) {
            List<Dependency> dependencies = project.getDependencies();
            for (Dependency dependency : dependencies) {
                if (dependency.getLocation("artifactId").getSource().getLocation().equals(project.getFile().getPath())) {
                    depManagementKeyToDep.computeIfAbsent(dependency.getManagementKey(), k -> new HashSet<>()).add(dependency.toString());
                    depToModules.computeIfAbsent(dependency.toString(), k -> new HashSet<>()).add(project);
                }
            }
        }
        appendToResult("========MavenConflictLibraryInModulesSmell========");
        for (String depMngKey : depManagementKeyToDep.keySet()) {
            Set<String> depSets = depManagementKeyToDep.get(depMngKey);
            if (depSets.size() == 1) {
                continue;
            }
            appendToResult("Dependency " + depMngKey + " has inconsistent versions between modules." );
            for (String dep : depSets) {
                appendToResult("    " + dep);
                for (MavenProject module : depToModules.get(dep)) {
                    appendToResult("        " + getRelativeModulePath(module, rootProject));
                }
            }
            appendToResult("---------");
        }

    }
}
