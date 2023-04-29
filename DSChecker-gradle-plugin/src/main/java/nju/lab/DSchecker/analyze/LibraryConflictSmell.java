package nju.lab.DSchecker.analyze;

import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;

//import org.aw.asm;
import neu.lab.conflict.container.DepJars;
import nju.lab.DSchecker.analyze.Conflict.ConflictJars;
import nju.lab.DSchecker.core.model.IDepJar;
import nju.lab.DSchecker.model.DepJar;


public class LibraryConflictSmell implements BaseSmell{
    public void detect() {
//        Set<NodeAdapter> conflictingNodes = NodeAdapters.i().getAllNodeAdapter()
//                .stream()
//                .filter( nodeAdapter -> { return !nodeAdapter.isNodeSelected();})
//                .collect(Collectors.toSet());
        Set<IDepJar> conflictingDepJars = DepJars.i().getAllDepJar()
                .stream()
                .filter(depJar -> {
                    return !depJar.isSelected();
                })
                .collect(Collectors.toSet());
        for (IDepJar depJar : conflictingDepJars) {
//            System.out.println(depJar.getJarFilePaths());
//            System.out.println(DepJars.i().getUsedJarPathsSeqForRisk(depJar));
            DepJar selectedJar = DepJars.i().getSelectedDepJarById(depJar.getName());
            ConflictJars.i().addConflictJar(selectedJar, depJar);
        }
        ConflictJars.i().printAllConflictJars();
    }
}

