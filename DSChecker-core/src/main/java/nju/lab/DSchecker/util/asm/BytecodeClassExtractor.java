package nju.lab.DSchecker.util.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BytecodeClassExtractor {
    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\target\\classes\\UnDeclared.class");

        // Read the .class file
        ClassReader reader = new ClassReader(fis);
        ClassNode node = new ClassNode();
        // Visit the .class file with the ClassNode
        reader.accept(node, 0);
        // Create a set to store the types
        Set<String> types = new HashSet<>();
        // Add the class name
        types.add(node.name);
        // Add the super class name
        types.add(node.superName);
        // Add the interface names
        for (String itf : node.interfaces) {
            types.add(itf);
        }
        // Add the field types
        for (FieldNode field : node.fields) {
            types.add(field.desc);
        }
        // Add the method return and parameter types
        for (MethodNode method : node.methods) {
            types.add(method.desc);
        }
        String regex = "L(.*?);";
        // Create a Pattern object from the regex
        Pattern pattern = Pattern.compile(regex);
        // Create a Matcher object from the descriptor and the pattern
        // Print the types
        for (String type : types) {
            Matcher matcher = pattern.matcher(type);
            while (matcher.find()) {
                // Print the matched group
                System.out.println(matcher.group(1));
            }
        }
    }

}
