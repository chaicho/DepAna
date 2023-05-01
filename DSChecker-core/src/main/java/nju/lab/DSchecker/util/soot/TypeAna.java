package nju.lab.DSchecker.util.soot;

import nju.lab.DSchecker.util.soot.tf.APITypeTransformer;
import soot.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TypeAna extends AbstractSootAna{
    private static TypeAna instance = new TypeAna();

    private TypeAna() {
    }

    public static TypeAna i() {
        if (instance == null) {
            instance = new TypeAna();
        }
        return instance;
    }

    /*
    Use the APITypeTransformer to get the ABI type of the AnalyzedClass
    */
    public void getABIType(List<String> JarPaths) {
        List<String> args = this.getArgsWithHostNoCg(JarPaths);
        APITypeTransformer apiTypeTransformer = new APITypeTransformer();
        PackManager.v().getPack("wjtp")
                .add(new Transform("wjtp.APIType", apiTypeTransformer));

        soot.Main.main(args.toArray(new String[0]));
    }
    public void analyze(List<String> JarPaths){


        List<String> args = this.getArgsWithHost(JarPaths);

        addExcludeArgs(args);
//        args.add("-no-bodies-for-excluded");
        System.out.println(args);
        APITypeTransformer apiTypeTransformer = new APITypeTransformer();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.APIType", apiTypeTransformer));
        hostProjectInfo.initABIDepjars(apiTypeTransformer.getABINames());

        try {
            soot.Main.main(args.toArray(new String[0]));
        }
        catch (Exception e){
            System.out.println(e);
        }

        SootClass mainClass = Scene.v().getMainClass();
        System.out.println("------mainClass------");
        System.out.println(mainClass);
        System.out.println("-----------EntryPoints-------------");
        System.out.println(Scene.v().getEntryPoints());
        System.out.println("---------------ArgumentClasses-------------");
        System.out.println(Scene.v().getApplicationClasses());
        System.out.println("---------------CallGraph-------------");
        System.out.println(Scene.v().getCallGraph().size());
        String callGraphString = Scene.v().getCallGraph().toString();

        try {
            FileWriter fileWriter = new FileWriter("CallGraph_Small.txt");
            fileWriter.write(callGraphString);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addCgArgs(List<String> argsList){
        argsList.add("-p");
        argsList.add("cg");
        argsList.add("verbose:true,all-reachable:true,implicit-entry:false");
//        argsList.add("implicit-entry:false,");
//        argsList.add("all-reachable:true");
//        argsList.add("exclude:java.*");
    }

    public static void main(String[] args){

//        getABIType("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\build\\classes\\java\\main");
    }

}
