package nju.lab.DScheckerGradle.model;

public class Method {
   private String methodSig;

   private String className;
    public Method(String methodSig) {
        this.methodSig = methodSig;
    }

    public Method(String signature, String declaringclassName) {
        this.methodSig = signature;
        this.className = declaringclassName;
    }


}
