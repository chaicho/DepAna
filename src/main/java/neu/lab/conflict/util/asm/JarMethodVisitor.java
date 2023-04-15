//package neu.lab.conflict.util.asm;
//
//import org.objectweb.asm.ClassReader;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Enumeration;
//import java.util.jar.JarEntry;
//import java.util.jar.JarFile;
//
//public class JarMethodVisitor {
//
//    public static void main(String[] args) throws IOException {
//        JarFile jarFile = new JarFile("path/to/your/jar/file.jar");
//        Enumeration<JarEntry> entries = jarFile.entries();
//        while (entries.hasMoreElements()) {
//            JarEntry jarEntry = entries.nextElement();
//            if (jarEntry.getName().endsWith(".class")) {
//                try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
//                    ClassReader classReader = new ClassReader(inputStream);
//                    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM5) {
//                        @Override
//                        public void visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//                            System.out.println("Method name: " + name);
//                        }
//                    };
//                    ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM5, methodVisitor) {};
//                    classReader.accept(classVisitor, 0);
//                }
//            }
//        }
//        jarFile.close();
//    }
//}
