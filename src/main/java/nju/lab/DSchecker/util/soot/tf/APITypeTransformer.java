package nju.lab.DSchecker.util.soot.tf;

import soot.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class APITypeTransformer extends SceneTransformer {


    private Set<Type> typesUsedInSuperClasses;
    private Set<Type> typesUsedInPublicFields;
    private Set<Type> typesUsedInPublicMethodParameters;

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {


        // Get the types used in super classes or interfaces
       typesUsedInSuperClasses = new HashSet<>();
        for (SootClass clazz : Scene.v().getClasses()) {
            if (clazz.hasSuperclass() && !clazz.isJavaLibraryClass()) {
                typesUsedInSuperClasses.addAll(clazz.getSuperclass().getInterfaces()
                        .stream()
                        .map(sootClass -> {return sootClass.getType();})
                        .collect(Collectors.toList()));

                typesUsedInSuperClasses.add(clazz.getSuperclass().getType());
//                System.out.println(clazz.getSuperclass().getType());
            }
        }

        // Get the types used in public method parameters, including generic parameter types
        typesUsedInPublicMethodParameters = new HashSet<>();
        for (SootClass clazz : Scene.v().getClasses()) {
            if(clazz.isJavaLibraryClass()){
                continue;
            }
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() || method.isProtected() || !method.isPrivate()) {
                    for (Type type : method.getParameterTypes()) {
                        typesUsedInPublicMethodParameters.add(type);
                        if (type instanceof RefType) {
                            RefType refType = (RefType) type;

                            Type refedType = refType.getSootClass().getType();
                            typesUsedInPublicMethodParameters.add(refedType);
                        } else if (type instanceof ArrayType) {
                            ArrayType arrayType = (ArrayType) type;
                            Type elementType = arrayType.getArrayElementType();
                            typesUsedInPublicMethodParameters.add(elementType);
                        }
                    }
                    Type returnType = method.getReturnType();
                    typesUsedInPublicMethodParameters.add(returnType);
                }
            }
        }


        // Get the types used in public fields
        typesUsedInPublicFields = new HashSet<>();
        for (SootClass clazz : Scene.v().getClasses()) {
            if(clazz.isJavaLibraryClass()){
                continue;
            }
            for (SootField field : clazz.getFields()) {
                if (field.isPublic() || field.isProtected() || !field.isPrivate()) {
                    typesUsedInPublicFields.add(field.getType());
                }
            }
        }

        // Get the public annotation types
//        Set<Type> publicAnnotationTypes = new HashSet<>();
//        for (SootClass clazz : Scene.v().getClasses()) {
//            if (clazz.isAnnotation() && (clazz.isPublic() || clazz.isProtected() || !clazz.isPrivate())) {
//                publicAnnotationTypes.add(clazz.getType());
//            }
//        }

        // Perform analysis using typesUsedInInternalClasses
        // ... (rest of the analysis code here)

//        System.out.println("TypesUsedInSuperClasses = " + typesUsedInSuperClasses);
//        System.out.println("TypesUsedInPublicMethodParameters = " + typesUsedInPublicMethodParameters);
//        System.out.println("TypesUsedInPublicFields = " + typesUsedInPublicFields);
    }
}
