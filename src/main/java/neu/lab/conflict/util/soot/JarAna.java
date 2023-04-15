package neu.lab.conflict.util.soot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.util.soot.tf.DsTransformer;

import neu.lab.conflict.vo.ClassVO;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Transform;

public class JarAna extends AbstractSootAna {
    public static long runtime = 0L;
    private static JarAna instance = new JarAna();

    private JarAna() {
    }

    public static JarAna i() {
        if (instance == null) {
            instance = new JarAna();
        }

        return instance;
    }

    public Map<String, ClassVO> deconstruct(List<String> jarFilePath) {
        long startTime = System.currentTimeMillis();
        List<String> args = this.getArgs((String[])jarFilePath.toArray(new String[0]));
        if (args.size() == 0) {
            return new HashMap();
        } else {
            DsTransformer transformer = new DsTransformer(jarFilePath);
            PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", transformer));
            SootUtil.getInstance().modifyLogOut();
            soot.Main.main(args.toArray(new String[0]));
            Map<String,ClassVO> clses = transformer.getClsTb();
            soot.G.reset();
            runtime = runtime + (System.currentTimeMillis() - startTime) / 1000;
            return  clses;
        }
    }

    public Map<String, ClassVO> deconstruct(List<String> jarFilePath, Set<String> clsSigs) {
        long startTime = System.currentTimeMillis();
        List<String> args = this.getArgs((String[])jarFilePath.toArray(new String[0]));
        if (args.size() == 0) {
            return new HashMap();
        } else {
            DsTransformer transformer = new DsTransformer(jarFilePath, clsSigs);
            PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", transformer));
            Main.main((String[])args.toArray(new String[0]));
            Map<String, ClassVO> clses = transformer.getClsTb();
            G.reset();
            runtime += (System.currentTimeMillis() - startTime) / 1000L;
            return clses;
        }
    }

    protected void addCgArgs(List<String> argsList) {
        argsList.addAll(Arrays.asList("-p", "cg", "off"));
    }
}
