package nju.lab.DSchecker.core.analyze;

import nju.lab.DSchecker.core.analyze.Conflict.ConflictJars;
import nju.lab.DSchecker.core.model.IDepJar;

import java.util.Set;
import java.util.stream.Collectors;


public class LibraryConflictSmell extends BaseSmell{
    public void detect() {
        Set<IDepJar> conflictingDepJars = depJars.getAllDepJar()
                .stream()
                .filter(depJar -> {
                    return !depJar.isSelected();
                })
                .collect(Collectors.toSet());
        for (IDepJar depJar : conflictingDepJars) {
//            System.out.println(depJar.getJarFilePaths());
//            System.out.println(DepJars.i().getUsedJarPathsSeqForRisk(depJar));
            IDepJar selectedJar = depJars.getSelectedDepJarById(depJar.getName());
            ConflictJars.i().addConflictJar(selectedJar, depJar);
        }
        ConflictJars.i().printAllConflictJars();
    }
}

