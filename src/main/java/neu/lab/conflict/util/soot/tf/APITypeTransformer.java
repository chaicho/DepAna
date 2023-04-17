package neu.lab.conflict.util.soot.tf;

import soot.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class APITypeTransformer extends SceneTransformer {


    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {


        // Get the types used in super classes or interfaces
        Set<Type> typesUsedInSuperClasses = new HashSet<>();
        for (SootClass clazz : Scene.v().getClasses()) {
            if (clazz.hasSuperclass()) {
                typesUsedInSuperClasses.addAll(clazz.getSuperclass().getInterfaces()
                        .stream()
                        .map(sootClass -> {return sootClass.getType();})
                        .collect(Collectors.toList()));

                typesUsedInSuperClasses.add(clazz.getSuperclass().getType());
            }
        }

// Get the types used in public method parameters, including generic parameter types
        Set<Type> typesUsedInPublicMethodParameters = new HashSet<>();
        for (SootClass clazz : Scene.v().getClasses()) {
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() || method.isProtected() || !method.isPrivate()) {
                    for (Type type : method.getParameterTypes()) {
                        typesUsedInPublicMethodParameters.add(type);
                        if (type instanceof RefType) {
                            RefType refType = (RefType) type;
//                            if (refType.isGeneric()) {
////                                String genericSignature = refType.getGenericSignature();
////                                if (genericSignature != null) {
////                                    // Extract generic type information from the generic signature
////                                    List<Type> actualTypeArguments = GenericSignatureParser.v()
////                                            .parseClassTypeSignature(genericSignature).getActualTypeArguments();
////                                    typesUsedInPublicMethodParameters.addAll(actualTypeArguments);
////                                }
////                            }
                            Type refedType = refType.getSootClass().getType();
                            typesUsedInPublicMethodParameters.add(refedType);
                        } else if (type instanceof ArrayType) {
                            ArrayType arrayType = (ArrayType) type;
                            Type elementType = arrayType.getArrayElementType();
                            typesUsedInPublicMethodParameters.add(elementType);
                        }
                    }
                }
            }
        }


        // Get the types used in public fields
        Set<Type> typesUsedInPublicFields = new HashSet<>();
        for (SootClass clazz : Scene.v().getClasses()) {
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
    }
}
