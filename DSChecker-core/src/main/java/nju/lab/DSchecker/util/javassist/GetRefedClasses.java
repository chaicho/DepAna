package nju.lab.DSchecker.util.javassist;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

public class GetRefedClasses{
    public static void main(String[] args) {
        // Input directory containing .class files
        String inputDir = "D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\target";

        // Analyze the referenced classes in the input directory
        Set<String> referencedClasses = analyzeReferencedClasses(inputDir);

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

    public static Set<String> analyzeReferencedClasses(String inputDir) {
        Set<String> referencedClasses = new HashSet<>();

        // Iterate over .class files and subdirectories in the input directory
        File dir = new File(inputDir);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    FileInputStream fis = null;
                    try {
                        // Load the .class file using FileInputStream
                        Set<String> tmpClasses = new HashSet<String>();
                        fis = new FileInputStream(file);
                        ClassFile cf = new ClassFile(new DataInputStream(fis));

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
                        // Get the referenced classes from the constant pool
                        Set<String> classes = constPool.getClassNames();
                        tmpClasses.addAll(classes);
                        for (String refClass : tmpClasses) {
                            // Add the referenced classes to the set
                            referencedClasses.add(refClass.replace('/', '.'));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (file.isDirectory()) {
                    // Recursively analyze the subdirectory and add its result to the set
                    referencedClasses.addAll(analyzeReferencedClasses(file.getPath()));
                }
            }
        }

        return referencedClasses;
    }

}
