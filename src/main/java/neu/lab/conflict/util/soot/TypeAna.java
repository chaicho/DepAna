package neu.lab.conflict.util.soot;

import neu.lab.conflict.util.soot.tf.APITypeTransformer;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public void getABIType(String projectPath) {



        List<String> args = this.getArgs(new String[]{projectPath});
        APITypeTransformer apiTypeTransformer = new APITypeTransformer();
        PackManager.v().getPack("wjtp")
                .add(new Transform("wjtp.APIType", apiTypeTransformer));
        soot.Main.main(args.toArray(new String[0]));

    }

    protected void addCgArgs(List<String> argsList) {
        argsList.addAll(Arrays.asList("-p", "cg", "off"));
    }
    public static void main(String[] args){

//        getABIType("D:\\Pathtodoc\\dependency-graph-as-task-inputs\\app\\build\\classes\\java\\main");
    }

}
