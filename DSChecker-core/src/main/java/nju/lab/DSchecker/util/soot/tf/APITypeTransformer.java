package nju.lab.DSchecker.util.soot.tf;

import lombok.Setter;
import soot.*;
import soot.util.Chain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class APITypeTransformer extends SceneTransformer {

    private Set<Type> typesUsedInSuperClasses;
    private Set<Type> typesUsedInPublicFields;
    private Set<Type> typesUsedInPublicMethodParameters;
    private Set<String> ABInames = new HashSet<>();

    public Set<String> getABINames(){
        return ABInames;
    }
    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        Chain<SootClass> classesToBeAnalyzed = Scene.v().getApplicationClasses();
        // Get the types used in super classes or interfaces
        System.out.println("APITypeTransformer");
        System.out.println("ClassesToBeAnalyzed = " + classesToBeAnalyzed);
        typesUsedInSuperClasses = new HashSet<>();

        // Get the types used in public method parameters, including generic parameter types
        typesUsedInPublicMethodParameters = new HashSet<>();

         typesUsedInPublicFields = new HashSet<>();
        for (SootClass clazz : classesToBeAnalyzed) {
            if(clazz.isJavaLibraryClass() || !clazz.isPublic() || clazz.getName().contains("$")){
                System.out.println("Skip Class = " + clazz);
                continue;
            }
            System.out.println("Currently in Class = " + clazz.getName());
            // Get the types used in super classes or interfaces
            if (clazz.hasSuperclass()) {
                typesUsedInSuperClasses.addAll(clazz.getSuperclass().getInterfaces()
                        .stream()
                        .map(sootClass -> {return sootClass.getType();})
                        .collect(Collectors.toList()));
                System.out.println("Interfaces = " + clazz.getSuperclass().getInterfaces());
                typesUsedInSuperClasses.add(clazz.getSuperclass().getType());
                
                System.out.println("SuperClass = " + clazz.getSuperclass().getType());

            }

            // Get the types used in public method parameters, including generic parameter types
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConcrete() && !method.getName().contains("lambda")) {
                    for (Type type : method.getParameterTypes()) {
                        typesUsedInPublicMethodParameters.add(type);
                        System.out.println("Method = " + method.getName() + " Type = " + type);
                        if (type instanceof ArrayType) {
                            ArrayType arrayType = (ArrayType) type;
                            Type elementType = arrayType.getArrayElementType();
                            typesUsedInPublicMethodParameters.add(elementType);
                            System.out.println("Method = " + method.getName() + " ArrayType = " + elementType);
                        }
                    }
                }
            }

            // Get the types used in public fields
            for (SootField field : clazz.getFields()) {
                if (field.isPublic()) {
                    typesUsedInPublicFields.add(field.getType());
                    System.out.println("Field = " + field.getName() + " Type = " + field.getType());
                }
            }
        }



//        Set<Type> publicAnnotationTypes = new HashSet<>();
//        for (SootClass clazz : classesToBeAnalyzed) {
//            if (clazz.isAnnotation() && (clazz.isPublic() || clazz.isProtected() || !clazz.isPrivate())) {
//                publicAnnotationTypes.add(clazz.getType());
//            }
//        }

        // Perform analysis using typesUsedInInternalClasses
        // ... (rest of the analysis code here)

//        System.out.println("TypesUsedInSuperClasses = " + typesUsedInSuperClasses);
//        System.out.println("TypesUsedInPublicMethodParameters = " + typesUsedInPublicMethodParameters);
//        System.out.println("TypesUsedInPublicFields = " + typesUsedInPublicFields);

        Set<Type> allABItypes = new HashSet<>();
        allABItypes.addAll(typesUsedInSuperClasses);
        allABItypes.addAll(typesUsedInPublicMethodParameters);
        allABItypes.addAll(typesUsedInPublicFields);
        for(Type type : allABItypes){
            if(type instanceof RefType){
                RefType refType = (RefType) type;
                ABInames.add(refType.getClassName());
            }
        }

//        System.out.println("AllABITypes = " + allABItypes);
    }
}
