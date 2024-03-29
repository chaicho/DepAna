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
    }
    public void addConflictJar(IDepJar selectedDepJar, IDepJar conflictJar) {
        if(selectedJar == null) {
            appendToResult("selectedJar is null");
        } else if (conflictJars == null) {
            appendToResult("conflictJars is null");
            return;
        }
        if (selectedDepJar == null || conflictJar ==null){
            return;
        }
        if (!selectedJar.containsKey(selectedDepJar.getSig())) {
            selectedJar.put(selectedDepJar.getSig(), selectedDepJar);
            conflictJars.put(selectedDepJar.getSig(), new HashSet<>(Arrays.asList(selectedDepJar, conflictJar)));
        }
        this.conflictJars.get(selectedDepJar.getSig()).add(conflictJar);
    }
    public void printAllConflictJars() {
        if(conflictJars.isEmpty())
            return;
        for (String key : conflictJars.keySet()) {
            log.warn("Selected jar: " + selectedJar.get(key).getJarFilePaths());
            log.warn("Conflict jars: " + conflictJars.get(key));
            appendToResult("Selected jar: " + selectedJar.get(key).getSig());
            appendToResult("Conflict jars: " + conflictJars.get(key));
            for (IDepJar depJar : conflictJars.get(key)) {
                appendToResult("    Pulled in by: " + depJar.getDepTrail());
            }
            appendToResult("---------");
        }
    }
    @Override
    public void detect() {
            appendToResult("========LibraryVersionConflictSmell========");
            Set<IDepJar> conflictingDepJars = depJars.getAllDepJar()
                .stream()
                .filter(depJar -> {
                    return !depJar.isSelected();
                })
                .collect(Collectors.toSet());
            if(conflictingDepJars.isEmpty()){
                return;
            }
            for (IDepJar depJar : conflictingDepJars) {
                IDepJar selectedJar = depJars.getSelectedDepJarById(depJar.getName());
                if (selectedJar == null){
//                    appendToResult("ERROR");
                    continue;
                }
                addConflictJar(selectedJar, depJar);
            }
            printAllConflictJars();
    }
}

