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

    public static void clear() {
        instance = null;
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
        APITypeTransformer apiTypeTransformer = new APITypeTransformer();
        if (PackManager.v().getPack("wjtp").get("wjtp.APIType") != null) {
            System.out.println("-------------Remove-----------");
            PackManager.v().getPack("wjtp").remove("wjtp.APIType");
        }
        try {
            PackManager.v().getPack("wjtp").add(new Transform("wjtp.APIType", apiTypeTransformer));
        } catch (Exception e){
            System.out.println("-------------ClearTransformer-----------");
            System.out.println(e);
        }
        try {
            System.out.println(args);
            soot.Main.main(args.toArray(new String[0]));
//            G.reset();
        } catch (Exception e){
            System.out.println("-------------Reset-----------");
            G.reset();
            System.out.println(e);
        }
        hostProjectInfo.initABIDepjars(apiTypeTransformer.getABINames());

    }

    @Override
    public void addCgArgs(List<String> argsList){
        argsList.add("-p");
        argsList.add("cg");
        argsList.add("verbose:true,all-reachable:true,implicit-entry:false");
        // argsList.add("-p");
        // argsList.add("cg.spark");
        // argsList.add("enabled:true");
//        argsList.add("verbose:true");
//        argsList.add("implicit-entry:false,");
//        argsList.add("all-reachable:true");
//        argsList.add("exclude:java.*");
    }

    public static void main(String[] args){

//        getABIType("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\build\\classes\\java\\main");
    }

}
