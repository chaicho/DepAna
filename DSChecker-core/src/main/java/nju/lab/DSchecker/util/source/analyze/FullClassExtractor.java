package nju.lab.DSchecker.util.source.analyze;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.Navigator;
import com.github.javaparser.resolution.declarations.ResolvedDeclaration;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FullClassExtractor {
    public static DepModel depModel;

    public static void setDepModel(DepModel depModel) {
        FullClassExtractor.depModel = depModel;

        Set<String> jarPaths = depModel.depJars.getUsedDepJarsPaths();
        Set<String> srcPaths = new HashSet<>(depModel.hostProjectInfo.getCompileSrcDirs());
        srcPaths.addAll(depModel.hostProjectInfo.getTestCompileSrcDirs());
        Set<JarTypeSolver> jarTypeSolvers = jarPaths.stream()
                .map(
                        jarPath -> {
                            try {
                                if (jarPath.endsWith("main")) {
                                    srcPaths.add(jarPath);
                                    return null;
                                }
                                else {
                                    return new JarTypeSolver(jarPath);
                                }
                            } catch (IOException e) {
                                System.out.println("jarPath: " + jarPath);
                                e.printStackTrace();
                                return null;
                            }
                        })
                .filter(x -> x != null)
                .collect(Collectors.toSet());

        Set<JavaParserTypeSolver> javaParserTypeSolvers = srcPaths.stream()
                .map(srcPath -> {
                    if (new File(srcPath).exists()) {
                        return new JavaParserTypeSolver(new File(srcPath));
                    }
                    return null;
                })
                .filter(x -> x != null)
                .collect(Collectors.toSet());

        CombinedTypeSolver myTypeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver());
        for (JarTypeSolver jarTypeSolver : jarTypeSolvers) {
            myTypeSolver.add(jarTypeSolver);
        }
        for (JavaParserTypeSolver javaParserTypeSolver : javaParserTypeSolvers) {
            myTypeSolver.add(javaParserTypeSolver);
        }
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(myTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        System.out.println("Symbol solver set up.");
    }

    // A method that takes a directory and recursively finds all the Java files
    public static Set<String> getClassesFromJavaFiles(String dirPath) {
        HashSet<String> referencedClassesInJavaFiles = new HashSet<String>();
            // Check if the file is a directory
            File dir = new File(dirPath);
            System.out.println(dirPath);
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
            Map<String, String> importNameToClass = new HashMap<>();
//           Get all the import statements
            cu.findAll(ImportDeclaration.class)
                    .stream()
                    .filter(importDeclaration -> !importDeclaration.isAsterisk())
                    .forEach( importDeclaration ->
                            importNameToClass.put(importDeclaration.getName().getIdentifier(),importDeclaration.getName().getQualifier().get().asString()
                            )
                    );
            cu.findAll(SimpleName.class).forEach(sn -> {
                try {
                    if (importNameToClass.containsKey(sn.asString())) {
                        referencedClassesInJavaFile.add(importNameToClass.get(sn.asString()) + "." + sn.asString() );

                    }
                }
                catch (Exception e) {
                    System.out.println("Exception in resolving SimpleName: " + sn.toString());
                }
            });        ;

//            Get all the annotations in the file.
            cu.findAll(AnnotationExpr.class).forEach(ad -> {
                try {
                    referencedClassesInJavaFile.add(ad.resolve().getQualifiedName());
                } catch (Exception e) {
                    System.out.println("Exception in resolving annotation: " + ad.toString());
                }
            });
            // Get all the classes in the file.
            cu.findAll(ClassOrInterfaceType.class).forEach(ct -> {
                try {
                    ResolvedType resolvedType = ct.resolve();
                    if (resolvedType.isReferenceType()) {
                        referencedClassesInJavaFile.add(resolvedType.asReferenceType().getQualifiedName());
                    } else if (resolvedType.isTypeVariable()) {
                        referencedClassesInJavaFile.add(resolvedType.asTypeVariable().qualifiedName());
                    }
                } catch (Exception e) {
                    System.out.println("Exception in resolving class: " + ct.toString());
                }
            });

            cu.findAll(MethodCallExpr.class).forEach(te -> {
                try {
                    if (te.getScope().isPresent()) {
                        ResolvedType resolvedType = te.getScope().get().calculateResolvedType();
                        if (resolvedType.isReferenceType()) {
                            referencedClassesInJavaFile.add(resolvedType.asReferenceType().getQualifiedName());
                        } else if (resolvedType.isTypeVariable()) {
                            referencedClassesInJavaFile.add(resolvedType.asTypeVariable().qualifiedName());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception while resolving method " + te.getScope());
                }
            });
            cu.findAll(FieldAccessExpr.class).forEach(fa -> {
                try {
                    ResolvedType resolvedType = fa.getScope().calculateResolvedType();
                    if (resolvedType.isReferenceType()) {
                        referencedClassesInJavaFile.add(resolvedType.asReferenceType().getQualifiedName());
                    } else if (resolvedType.isTypeVariable()) {
                        referencedClassesInJavaFile.add(resolvedType.asTypeVariable().qualifiedName());
                    }
                } catch (Exception e) {
                    System.out.println("Exception in resolving field access: " + fa.toString());
                }
            });
        } catch (FileNotFoundException e) {
            System.out.println("Exception in getReferencesFromJavaFile: " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError in getReferencesFromJavaFile: " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception in getReferencesFromJavaFile: " + file.getAbsolutePath());
            e.printStackTrace();
        }

        return referencedClassesInJavaFile;
    }

    public static void main(String[] args) {
        Set<String> classes = getReferencesFromJavaFile(new File("/root/dependencySmell/evaluation/realProjects/gradle/projectsDir/sonarqube-community-branch-plugin/sonarqube-community-branch-plugin-1.14.0/src/main/java/com/github/mc1arke/sonarqube/plugin/almclient/github/v3/RestApplicationAuthenticationProvider.java"));
        for (String className : classes) {
            System.out.println(className);
        }
    }
}
