package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class LibraryConflictSmell extends BaseSmell{
    HashMap<String, Set<IDepJar>> conflictJars = new HashMap<>() ;
    HashMap<String, IDepJar> selectedJar = new HashMap<>();

    public LibraryConflictSmell() {
        super();
        conflictJars = new HashMap<>();
        selectedJar = new HashMap<>();
//        System.out.println("Initial");
    }
    public void addConflictJar(IDepJar selectedDepJar, IDepJar conflictJar) {

        if (!selectedJar.containsKey(selectedDepJar.getName())) {
            selectedJar.put(selectedDepJar.getName(), selectedDepJar);
            conflictJars.put(selectedDepJar.getName(), new HashSet<>(Arrays.asList(selectedDepJar, conflictJar)));
        }
        this.conflictJars.get(selectedDepJar.getName()).add(conflictJar);
    }
    public void printAllConflictJars() {
        if(conflictJars.isEmpty())
            return;
        for (String key : conflictJars.keySet()) {
            log.warn("selected jar: " + selectedJar.get(key).getJarFilePaths());
            log.warn("conflict jars: " + conflictJars.get(key));
            output("selected jar: " + selectedJar.get(key).getJarFilePaths());
            output("conflict jars: " + conflictJars.get(key));
        }
    }
    public void detect() {
        output("========LibraryConflictSmell========");
        log.warn("=======Jar Conflict Smell========");
        Set<IDepJar> conflictingDepJars = depJars.getAllDepJar()
                .stream()
                .filter(depJar -> {
                    return !depJar.isSelected();
                })
                .collect(Collectors.toSet());
        for (IDepJar depJar : conflictingDepJars) {
            IDepJar selectedJar = depJars.getSelectedDepJarById(depJar.getName());
            addConflictJar(selectedJar, depJar);
        }
        printAllConflictJars();
    }
}

