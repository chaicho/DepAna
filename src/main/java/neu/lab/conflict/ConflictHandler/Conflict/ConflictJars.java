package neu.lab.conflict.ConflictHandler.Conflict;

import neu.lab.conflict.vo.DepJar;

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
        for (String key : conflictJars.keySet()) {
            System.out.println("selected jar: " + selectedJar.get(key).getJarFilePaths());
            System.out.println("conflict jars: " + conflictJars.get(key));
        }
    }
}
