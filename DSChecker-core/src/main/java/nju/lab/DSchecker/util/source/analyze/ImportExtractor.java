package nju.lab.DSchecker.util.source.analyze;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;

public class ImportExtractor {
    // A method that takes a directory and recursively finds all the Java files
    public static Set<String> getImportsFromJavaFiles(File dir) {
        // Check if the file is a directory
        HashSet<String> importClassesInJavaFiles = new HashSet<String>();
        if (dir.isDirectory()) {
            // Get the files in the directory
            File[] files = dir.listFiles();
            // Loop through the files
            for (File file : files) {
                // If the file is a directory, call the method recursively
                if (file.isDirectory()) {
                    importClassesInJavaFiles.addAll(getImportsFromJavaFiles(file));
                }
                // If the file is a Java file, parse it and extract the imports
                else if (file.getName().endsWith(".java")) {
                    importClassesInJavaFiles.addAll(getImportsFromJavaFile(file));
                }
            }
        }
        return importClassesInJavaFiles;
    }

    // A method that parses a Java file and extracts the import statements
    public static Set<String> getImportsFromJavaFile(File file) {
        Set<String> result = new HashSet<String>();
        try {
            // Parse the file using JavaParser
            CompilationUnit cu = StaticJavaParser.parse(file);
            // Get the list of import declarations
            List<ImportDeclaration> importDeclarations = cu.getImports();
            // Loop through the import declarations
            for (ImportDeclaration importDeclaration : importDeclarations) {
                // Get the name of the import
                String importName = importDeclaration.getNameAsString();
                // Add it to the list of imports
                result.add(importName);
            }
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        File dir = new File("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\src\\main");
        Set<String> importClassesInJavaFiles = getImportsFromJavaFiles(dir);
        for (String importClass : importClassesInJavaFiles) {
            System.out.println(importClass);
        }
    }
}
