package nju.lab.DScheckerMaven.core.analyze;

import nju.lab.DSchecker.core.analyze.BaseSmell;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MavenLibraryScopeConflictSmell extends BaseSmell {
    @Override
    public void detect() {
        Set<? extends IDepJar> allDepJars = depJars.getAllDepJar();
        HashMap<String, Set<String>> scopeMap = new HashMap<>();
        HashMap<String, String> selectedScopeMap = new HashMap<>();
        HashMap<String, Set<IDepJar>> nameToDepJarMap = new HashMap<>();
        for (IDepJar jar : depJars.getUsedDepJars()) {
            selectedScopeMap.put(jar.getName(), jar.getScope());
        }
        for (IDepJar jar : allDepJars) {
            if (scopeMap.get(jar.getName()) == null) {
                scopeMap.put(jar.getName(), new HashSet<>());
                nameToDepJarMap.put(jar.getName(), new HashSet<>());
            }
            scopeMap.get(jar.getName()).addAll(jar.getScopes());
            nameToDepJarMap.get(jar.getName()).add(jar);
        }
        appendToResult("========LibraryScopeConflictSmell========");
        for (String name : scopeMap.keySet()) {
            if (scopeMap.get(name).size() > 1) {
                appendToResult("Library name: " + name);
                appendToResult("Declared Scopes: " + scopeMap.get(name));
                appendToResult("Final Scope: " + selectedScopeMap.get(name));
                appendToResult("Affected jars: ");
                Set<String> allImportPaths = new HashSet<String>();
                for (IDepJar jar : nameToDepJarMap.get(name)) {
                    allImportPaths.addAll(jar.getDepTrails());
                }
                for (String path : allImportPaths) {
                    appendToResult("    " + path.replace("\n",""));
                }
                appendToResult("---------");
            }
        }
    }
}
