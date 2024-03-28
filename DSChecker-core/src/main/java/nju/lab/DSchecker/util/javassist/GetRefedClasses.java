package nju.lab.DSchecker.util.javassist;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public class GetRefedClasses{
    public static void main(String[] args) {
        // Input directory containing .class files
        String inputDir = "C:\\Users\\DELL\\OneDrive\\dependency-graph-as-task-inputs\\app\\target\\classes";
        String classDir =  "C:\\Users\\DELL\\OneDrive\\dependency-graph-as-task-inputs\\app\\target\\classes\\App.class";
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
    public static Set<String> analyzeReferencedClassFromFile(String file) {
        Set<String> tmpClasses = new HashSet<>();
        Set<String> retClasses = new HashSet<>();
        try {
            FileInputStream fis = new FileInputStream(file);
            ClassFile cf = new ClassFile(new DataInputStream(fis));
            // Get the annotations attributes
            AnnotationsAttribute invisibleAnnotations = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);

            // Create a set to store annotations class names
            Set<String> annotationsClasses = new HashSet<>();

            // Check and add annotations class names
            if (invisibleAnnotations != null) {
                for (Annotation ann : invisibleAnnotations.getAnnotations()) {
                    annotationsClasses.add(ann.getTypeName());
                }
            }

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
                if (!annotationsClasses.contains(formalizedClass)) {
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

}
