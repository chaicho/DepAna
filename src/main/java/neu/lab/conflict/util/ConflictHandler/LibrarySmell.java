package neu.lab.conflict.util.ConflictHandler;

import java.io.*;

//import org.aw.asm;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.container.NodeAdapters;
import org.gradle.jvm.tasks.Jar;
import org.objectweb.asm.*;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LibrarySmell {
    private static LibrarySmell instance;

    private LibrarySmell() {
        // private constructor to enforce singleton pattern
    }

    public static synchronized LibrarySmell getInstance() {
        if (instance == null) {
            instance = new LibrarySmell();
        }
        return instance;
    }
    public void init(DepJars depJars, NodeAdapters nodeAdapters){

    }
    public void detect(){

    }
    public static void main(String[] args) throws IOException {
        LibrarySmell librarySmell = LibrarySmell.getInstance();
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
