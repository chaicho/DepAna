package nju.lab.DSchecker.util.javassist;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public class GetRefedClasses{

    public static void main(String[] args) {
        // Input directory containing .class files
        String inputDir = "C:\\Users\\DELL\\OneDrive\\dependency-graph-as-task-inputs\\app\\target\\classes";
//        String classDir =  "C:\\Users\\DELL\\OneDrive\\dependency-graph-as-task-inputs\\app\\target\\classes\\App.class";
//        String classDir = "/root/dependencySmell/evaluation/realProjects/gradle/projectsDir/dropwizard-guicey/dropwizard-guicey-5.9.3/dropwizard-guicey/build/classes/java/main/ru/vyarus/dropwizard/guice/debug/LifecycleDiagnostic$JerseyEventListener.class";
//        String classDir = "/root/dependencySmell/evaluation/realProjects/gradle/projectsDir/gtfs-validator/gtfs-validator-4.2.0/core/build/classes/java/main/org/mobilitydata/gtfsvalidator/table/GtfsColumnDescriptor$Builder.class";
//        String classDir = "/root/dependencySmell/evaluation/realProjects/gradle/projectsDir/dropwizard-guicey/dropwizard-guicey-5.9.3/dropwizard-guicey/build/classes/java/main/ru/vyarus/dropwizard/guice/debug/LifecycleDiagnostic.class"
        String classDir = "/root/dependencySmell/evaluation/realProjects/gradle/projectsDir/dropwizard-guicey/dropwizard-guicey-5.9.3/dropwizard-guicey/build/classes/java/main/ru/vyarus/dropwizard/guice/test/GuiceyTestSupport.class";
        // Analyze the referenced classes in the input directory
//        Set<String> referencedClasses = analyzeReferencedClasses(inputDir);
        Set<String> referencedClasses = analyzeReferencedClassFromFile(classDir);

        // Print the referenced classes
        System.out.println("Referenced classes:");
        for (String refClass : referencedClasses) {
            System.out.println(refClass);
        }
    }
    public static Set<String> getFullQualifiedNameFromStr(String name) {
        Set<String> names = new HashSet<String>();
        final String regex = "L(.*?);";
        // Create a Pattern object from the regex
        final Pattern pattern = Pattern.compile(regex);
        // Create a Matcher object from the descriptor and the pattern
        Matcher matcher = pattern.matcher(name);
        while (matcher.find()) {
            // Print the matched group
            names.add(matcher.group(1));
        }
        return names;
    }

    public static boolean containsSameClass(Set<String> strings, String targetString) {
        for (String string: strings){
            if (string.contains(targetString) || targetString.startsWith(string)) {
                return true;
            }
        }
//        if (targetString.)
        return false;
    }

    public static Set<String> analyzeReferencedClassFromFile(String file) {
        Set<String> tmpClasses = new HashSet<>();
        Set<String> retClasses = new HashSet<>();
        try {
            FileInputStream fis = new FileInputStream(file);
            ClassFile cf = new ClassFile(new DataInputStream(fis));
            // Get the annotations attributes

            // Create a set to store annotations class names
            Set<String> annotationsClasses = AnnotationUsageFinder.findAnnotations(file);


            // Get the constant pool of the loaded class
            ConstPool constPool = cf.getConstPool();
            for (int i = 1; i < constPool.getSize(); i++) {
                // Check if the entry is a UTF-8 constant
                if (constPool.getTag(i) == ConstPool.CONST_Utf8) {
                    // Get the UTF-8 value
                    String value = constPool.getUtf8Info(i);
                    // Print the index and the value
                    tmpClasses.addAll(getFullQualifiedNameFromStr(value));
                }
            }

            Set<String> classes = constPool.getClassNames();
            tmpClasses.addAll(classes);

            for (String refClass : tmpClasses) {
                // Add the referenced classes to the set
                String formalizedClass = refClass.replace('/', '.');
                if (!containsSameClass(annotationsClasses, formalizedClass)) {
                    retClasses.add(formalizedClass);
                }
            }

            // Get the referenced classes from the constant pool

        } catch (Exception e) {
            e.printStackTrace();
        }
        return retClasses;
    }
    public static Set<String> analyzeReferencedClasses(String inputDir) {
        Set<String> referencedClasses = new HashSet<>();

        // Iterate over .class files and subdirectories in the input directory
        File dir = new File(inputDir);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    // Load the .class file using FileInputStream
                    Set<String> tmpClasses = analyzeReferencedClassFromFile(file.getAbsolutePath());
                    referencedClasses.addAll(tmpClasses);
                } else if (file.isDirectory()) {
                    // Recursively analyze the subdirectory and add its result to the set
                    referencedClasses.addAll(analyzeReferencedClasses(file.getPath()));
                }
            }
        }

        return referencedClasses;
    }



    public static class AnnotationUsageFinder {

        public  void main(String[] args) {
            if (args.length != 1) {
                System.err.println("Usage: java AnnotationUsageFinder <classFile>");
                return;
            }

            String classFile = args[0];
            try {
                Set<String> allAnnotations = findAnnotations(classFile);
                for (String annotation : allAnnotations) {
                    System.out.println(annotation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static Set<String> findAnnotations(String classFile) throws IOException {
            ClassPool pool = ClassPool.getDefault();
            FileInputStream fileInputStream = new FileInputStream(classFile);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            CtClass ctClass = pool.makeClass(dataInputStream);
            fileInputStream.close();

            Set<String> allAnnotations = new HashSet<>();

            // Check annotations on the class itself
            allAnnotations.addAll(getAnnotations(ctClass));

            // Check annotations on fields
            for (CtField field : ctClass.getDeclaredFields()) {
                allAnnotations.addAll(getAnnotations(field));
            }

            // Check annotations on methods
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                allAnnotations.addAll(getAnnotations(method));
            }
            allAnnotations.add("com.google.errorprone.annotations.");
            allAnnotations.add("org.immutables.value.");
            allAnnotations.add("javax.annotation");
            allAnnotations.add("edu.umd.cs.findbugs.annotations");
            return allAnnotations;
        }

        private static Set<String> getAnnotations(CtClass ctClass) {
            Set<String> annotations = new HashSet<>();
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            if (annotationsAttribute != null) {
                for (Annotation annotation : annotationsAttribute.getAnnotations()) {
                    annotations.add(annotation.getTypeName());
                }
            }
            return annotations;
        }

        private static Set<String> getAnnotations(CtField field) {
            Set<String> annotations = new HashSet<>();
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
            if (annotationsAttribute != null) {
                for (Annotation annotation : annotationsAttribute.getAnnotations()) {
                    annotations.add(annotation.getTypeName());
                }
            }
            return annotations;
        }

        private static Set<String> getAnnotations(CtMethod method) {
            Set<String> annotations = new HashSet<>();
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
            if (annotationsAttribute != null) {
                for (Annotation annotation : annotationsAttribute.getAnnotations()) {
                    annotations.add(annotation.getTypeName());
                }
            }
            return annotations;
        }
    }
}
