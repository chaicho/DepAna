package nju.lab.DSchecker.analyze;

import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;

//import org.aw.asm;
import neu.lab.conflict.container.DepJars;
import nju.lab.DSchecker.analyze.Conflict.ConflictJars;
import nju.lab.DSchecker.model.DepJar;


public class LibrarySmell {
    private static LibrarySmell instance;

    private LibrarySmell() {
        // private constructor to enforce singleton pattern
    }

    public static synchronized LibrarySmell i() {
        if (instance == null) {
            instance = new LibrarySmell();
        }
        return instance;
    }
    public void detect(){
//        Set<NodeAdapter> conflictingNodes = NodeAdapters.i().getAllNodeAdapter()
//                .stream()
//                .filter( nodeAdapter -> { return !nodeAdapter.isNodeSelected();})
//                .collect(Collectors.toSet());
        Set<DepJar>  conflictingDepJars = DepJars.i().getAllDepJar()
                .stream()
                .filter( depJar -> { return !depJar.isSelected();})
                .collect(Collectors.toSet());
        for (DepJar depJar : conflictingDepJars) {
//            System.out.println(depJar.getJarFilePaths());
//            System.out.println(DepJars.i().getUsedJarPathsSeqForRisk(depJar));
            DepJar selectedJar =  DepJars.i().getSelectedDepJarById(depJar.getName());
            ConflictJars.i().addConflictJar(selectedJar,depJar);
        }
        ConflictJars.i().printAllConflictJars();
    }
    public static void main(String[] args) throws IOException {
        LibrarySmell librarySmell = LibrarySmell.i();
        String jarPath1 = "D:\\Gradles\\.gradle\\caches\\modules-2\\files-2.1\\org.apache.commons\\commons-collections4\\4.4\\62ebe7544cb7164d87e0637a2a6a2bdc981395e8\\commons-collections4-4.4.jar";
        String jarPath2 = "D:\\Gradles\\.gradle\\caches\\modules-2\\files-2.1\\org.apache.commons\\commons-collections4\\4.2\\54ebea0a5b653d3c680131e73fe807bb8f78c4ed\\commons-collections4-4.2.jar";
        System.out.println("Hello World!");
        try{
//            librarySmell.analyzeJars(jarPath1, jarPath2);
        }
        catch (Exception e){
            System.out.println(e.toString());
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
        }
    }
}
