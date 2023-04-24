package nju.lab.DSchecker.analyze.Conflict;

import nju.lab.DSchecker.model.DepJar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class ConflictJars {

    HashMap<String, Set<DepJar>> conflictJars;
    HashMap<String, DepJar> selectedJar;

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

    public void addConflictJar(DepJar selectedDepJar, DepJar conflictJar) {
        if (!selectedJar.containsKey(selectedDepJar.getName())) {
            selectedJar.put(selectedDepJar.getName(), selectedDepJar);
            conflictJars.put(selectedDepJar.getName(), new HashSet<>(Arrays.asList(selectedDepJar, conflictJar)));
        }
        this.conflictJars.get(selectedDepJar.getName()).add(conflictJar);
    }
    public void printAllConflictJars() {
        System.out.println("Jar Conflict Smell:");
        for (String key : conflictJars.keySet()) {
            System.out.println("selected jar: " + selectedJar.get(key).getJarFilePaths());
            System.out.println("conflict jars: " + conflictJars.get(key));
        }
    }
}
