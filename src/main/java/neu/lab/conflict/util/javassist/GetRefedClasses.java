package neu.lab.conflict.util.javassist;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

public class GetRefedClasses{
    public static void main(String[] args) {
        // Input directory containing .class files
        String inputDir = "D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\build\\classes\\java\\main";

        // Analyze the referenced classes in the input directory
        Set<String> referencedClasses = analyzeReferencedClasses(inputDir);

        // Print the referenced classes
        System.out.println("Referenced classes:");
        for (String refClass : referencedClasses) {
            System.out.println(refClass);
        }
    }

    public static Set<String> analyzeReferencedClasses(String inputDir) {
        Set<String> referencedClasses = new HashSet<>();

        // Iterate over .class files in the input directory
        File dir = new File(inputDir);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    FileInputStream fis = null;
                    try {
                        // Load the .class file using FileInputStream
                        fis = new FileInputStream(file);
                        ClassFile cf = new ClassFile(new DataInputStream(fis));

                        // Get the constant pool of the loaded class
                        ConstPool constPool = cf.getConstPool();

                        // Get the referenced classes from the constant pool
                        Set<String> classes = constPool.getClassNames();
                        for (String refClass : classes) {
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
                }
            }
        }

        return referencedClasses;
    }
}
