package nju.lab.DSchecker.util.soot.tf;

import neu.lab.conflict.util.SootUtil;
import nju.lab.DSchecker.model.ClassVO;
import soot.SceneTransformer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DsTransformer extends SceneTransformer {
    private Map<String, ClassVO> clsTb;
    private List<String> jarPaths;
    private Set<String> clsSigs;
    public DsTransformer(List<String> jarPaths) {
        this.jarPaths = jarPaths;
    }
    public DsTransformer(List<String> jarPaths, Set<String> clsSigs) {
        this.jarPaths = jarPaths;
        this.clsSigs = clsSigs;
    }
    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        if (clsSigs != null) {
            clsTb = SootUtil.getInstance().getClassTb(this.jarPaths, clsSigs);
        } else {
            clsTb = SootUtil.getInstance().getClassTb(this.jarPaths);
        }
    }
    public Map<String, ClassVO> getClsTb() {
        return clsTb;
    }
}