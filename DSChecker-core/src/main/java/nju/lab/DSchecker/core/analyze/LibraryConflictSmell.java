package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static soot.jbco.IJbcoTransform.output;


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
            output("selectedJar is null");
        } else if (conflictJars == null) {
            output("conflictJars is null");
            return;
        }
        if (selectedDepJar == null || conflictJar ==null){
            return;
        }
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
    @Override
    public void detect() {
            output("========LibraryConflictSmell========");
            log.warn("=======Jar Conflict Smell========");
            Set<IDepJar> conflictingDepJars = depJars.getAllDepJar()
                    .stream()
                    .filter(depJar -> {
                        return !depJar.isSelected();
                    })
                    .collect(Collectors.toSet());
            output("========UnSelectedJars======== ");
            output(conflictingDepJars.toString());
            if(conflictingDepJars.isEmpty()){
                return;
            }
            for (IDepJar depJar : conflictingDepJars) {
                IDepJar selectedJar = depJars.getSelectedDepJarById(depJar.getName());
                if (selectedJar == null){
                    output("ERROR");
                }
                addConflictJar(selectedJar, depJar);
            }
            printAllConflictJars();



    }
}

