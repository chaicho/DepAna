package nju.lab.DSchecker.core.analyze.Conflict;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ConflictJars {

    HashMap<String, Set<IDepJar>> conflictJars;
    HashMap<String, IDepJar> selectedJar;

    private static ConflictJars instance;
    private ConflictJars() {
        conflictJars = new HashMap<>();
        selectedJar = new HashMap<>();
    }
    public static ConflictJars i() {
        if (instance == null) {
            instance = new ConflictJars();
        }
        return instance;
    }

    public void addConflictJar(IDepJar selectedDepJar, IDepJar conflictJar) {
        if (!selectedJar.containsKey(selectedDepJar.getSig())) {
            selectedJar.put(selectedDepJar.getSig(), selectedDepJar);
            conflictJars.put(selectedDepJar.getSig(), new HashSet<>(Arrays.asList(selectedDepJar, conflictJar)));
        }
        this.conflictJars.get(selectedDepJar.getSig()).add(conflictJar);
    }
    public void printAllConflictJars() {
        if(conflictJars.isEmpty())
            return;
        log.warn("Jar Conflict Smell:");
        for (String key : conflictJars.keySet()) {
            log.warn("selected jar: " + selectedJar.get(key).getJarFilePaths());
            log.warn("conflict jars: " + conflictJars.get(key));
        }
    }
}
