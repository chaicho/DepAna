package nju.lab.DSchecker.util.soot.tf;

import soot.EntryPoints;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AddEntryTransformer extends SceneTransformer {
    List<SootMethod> specified_entrys;
    Collection<String> specified_sootMethods;
    public AddEntryTransformer() {
        specified_sootMethods = new ArrayList<>();
    }
    public AddEntryTransformer(Collection<String> specified_entrys) {
        specified_sootMethods = specified_entrys;
        if (specified_entrys == null) {
            specified_sootMethods = new ArrayList<>();
        }
    }
    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        specified_entrys = new ArrayList<>();
        for (String sig : specified_sootMethods) {
            for (SootMethod appMthd : EntryPoints.v().methodsOfApplicationClasses()) {
                if (sig.equals(appMthd.getSignature())) {
                    if (appMthd.isPrivate()) {
                        continue;
                    }
                    this.specified_entrys.add(appMthd);
                }
            }
        }
        Scene.v().setEntryPoints(specified_entrys);
        //Options.v().setPhaseOption("cg.spark", "enabled:true");
    }
}
