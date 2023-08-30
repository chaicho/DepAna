package nju.lab.DSchecker.util.source.analyze;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.Navigator;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import nju.lab.DSchecker.core.model.DepModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FullClassExtractor {
    public static DepModel depModel;
    public static void setDepModel(DepModel depModel) {
        FullClassExtractor.depModel = depModel;

        Set<String> jarPaths = depModel.depJars.getUsedDepJarsPaths();
        Set<String> srcPaths = depModel.hostProjectInfo.getCompileSrcDirs();
        srcPaths.addAll(depModel.hostProjectInfo.getTestCompileSrcDirs());
        Set<JarTypeSolver> jarTypeSolvers = jarPaths.stream()
                .map(
                        jarPath -> {
                            try {
                                return new JarTypeSolver(jarPath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .collect(Collectors.toSet());

        Set<JavaParserTypeSolver> javaParserTypeSolvers = srcPaths.stream()
                .map(srcPath -> new JavaParserTypeSolver(new File(srcPath)))
                .collect(Collectors.toSet());

        CombinedTypeSolver myTypeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver()
        );
        for (JarTypeSolver jarTypeSolver : jarTypeSolvers) {
            myTypeSolver.add(jarTypeSolver);
        }
        for (JavaParserTypeSolver javaParserTypeSolver : javaParserTypeSolvers) {
            myTypeSolver.add(javaParserTypeSolver);
        }
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }
    // A method that takes a directory and recursively finds all the Java files
    public static Set<String> getClassesFromJavaFiles(String dirPath) {
        // Check if the file is a directory
        HashSet<String> referencedClassesInJavaFiles = new HashSet<String>();
        File dir = new File(dirPath);
        if (dir.isDirectory()) {
            // Get the files in the directory
            File[] files = dir.listFiles();
            // Loop through the files
            for (File file : files) {
                // If the file is a directory, call the method recursively
                if (file.isDirectory()) {
                    referencedClassesInJavaFiles.addAll(getClassesFromJavaFiles(file.getAbsolutePath()));
                }
                // If the file is a Java file, parse it and extract the imports
                else if (file.getName().endsWith(".java")) {
                    referencedClassesInJavaFiles.addAll(getReferencesFromJavaFile(file));
                }
            }
        }
        return referencedClassesInJavaFiles;
    }

    // A method that parses a Java file and extracts the import statements
    public static Set<String> getReferencesFromJavaFile(File file) {
        Set<String> referencedClassesInJavaFile = new HashSet<String>();
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
//            Get all the annotations in the file.
            cu.findAll(AnnotationExpr.class).forEach(ad -> {
                referencedClassesInJavaFile.add(ad.resolve().getQualifiedName());
            });
//            Get all the classes in the file.
            cu.findAll(ClassOrInterfaceType.class).forEach(ct -> {
                ResolvedType resolvedType = ct.resolve();
                if (resolvedType.isReferenceType()) {
                    referencedClassesInJavaFile.add(resolvedType.asReferenceType().getQualifiedName());
                }
                else if (resolvedType.isTypeVariable()) {
                    referencedClassesInJavaFile.add(resolvedType.asTypeVariable().qualifiedName());
                }
            });
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return referencedClassesInJavaFile;
    }
    public static void main(String[] args) {
       Set<String> classes = getClassesFromJavaFiles("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\src\\main");
       for (String className : classes){
              System.out.println(className);
        }
    }
}
