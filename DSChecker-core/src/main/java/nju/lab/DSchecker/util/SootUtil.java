package nju.lab.DSchecker.util;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import nju.lab.DSchecker.core.model.ClassVO;
import nju.lab.DSchecker.core.model.MethodVO;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.jimple.toolkits.callgraph.CallGraph;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
//import org.apache.
/**
 * @author asus
 * @author guoruijie
 */


@Slf4j
public class SootUtil {

    /**
     * @param mthdSig
     *            e.g.:<org.slf4j.event.SubstituteLoggingEvent: org.slf4j.event.Level
     *            getLevel()>
     * @return e.g.: org.slf4j.event.Level getLevel();
     */
    public static String mthdSig2name(String mthdSig) {
        return mthdSig.substring(mthdSig.indexOf(":") + 1, mthdSig.indexOf(")") + 1);
    }
    public String mthdSig2Onlyname(String mthdSig) {
        return mthdSig.substring(mthdSig.indexOf(":") + 2, mthdSig.indexOf("(")).split(" ")[1];
    }
    /**
     * @param mthdSig
     *            e.g.:<org.slf4j.event.SubstituteLoggingEvent: org.slf4j.event.Level
     *            getLevel()>
     * @return e.g.:org.slf4j.event.Level getLevel();
     */
    public String mthdSig2nameRemoveTheFirstSpace(String mthdSig) {
        return mthdSig.substring(mthdSig.indexOf(":") + 2, mthdSig.indexOf(")") + 1);
    }
    /**
     * @param mthdSig
     *            e.g.:<org.slf4j.event.SubstituteLoggingEvent: org.slf4j.event.Level
     *            getLevel()>
     * @return e.g.:org.slf4j.event.SubstituteLoggingEvent
     */
    public String mthdSig2cls(String mthdSig) {
        return mthdSig.substring(1, mthdSig.indexOf(":"));
    }
    public String cls2RootCls(String cls) {
        if (cls.contains("$")) {
            return cls.substring(0, cls.indexOf("$"));
        }
        return cls;
    }
    /**
     * 获取这些jar包中所有定义的类
     * @param paths
     * @return
     */
    public static Set<String> getJarsClasses(List<String> paths) {
        Set<String> allCls = new HashSet<String>();
        for (String path : paths) {
            allCls.addAll(getJarClasses(path));
        }
        return allCls;
    }


    /**
     * 获取指定jar包或目录下所有定义的类
     * @param path 指定jar包路径或指定目录路径
     * @return
     */
    public static List<String> getJarClasses(String path) {
        if (new File(path).exists()) {
            if ((new File(path).isFile() && path.endsWith("jar")) || new File(path).isDirectory()) {
                return SourceLocator.v().getClassesUnder(path);
            } else {
                log.error(path + "is illegal classpath");
            }

        } else {
            log.error(path + "doesn't exist in local");
        }
        return new ArrayList<String>();
    }
    /**
     * 获取指定jar包或目录下所有定义的类（不调用MavenLog）
     * @param path
     * @return
     */
    public List<String> getJarClassesNoLog(String path) {
        if (new File(path).exists()) {
            if (!path.endsWith("tar.gz") && !path.endsWith(".pom") && !path.endsWith(".war")) {
                return SourceLocator.v().getClassesUnder(path);
            } else {
                log.error(path + "is illegal path");
            }
        } else {
            log.error(path + "doesn't exist in local");
        }
        return new ArrayList<String>();
    }
    /**
     *
     * @param jarPaths
     * @return
     */
    @Deprecated
    public static Map<String, ClassVO> getClassTb(List<String> jarPaths) {
        Map<String, ClassVO> clsTb = new HashMap<String, ClassVO>();
        for (String clsSig : SootUtil.getJarsClasses(jarPaths)) {
            SootClass sootClass = Scene.v().getSootClass(clsSig);
            ClassVO clsVO = new ClassVO(sootClass.getName());
            clsTb.put(sootClass.getName(), clsVO);
            for (SootMethod sootMethod : sootClass.getMethods()) {
                    clsVO.addMethod(new MethodVO(sootMethod.getSignature(), clsVO));
            }
        }

        return clsTb;
    }
    /**
     * 已有类的情况下
     * @param jarPaths
     * @param clsSigs
     * @return
     */
    public static Map<String, ClassVO> getClassTb(List<String> jarPaths, Set<String> clsSigs) {
        Map<String, ClassVO> clsTb = new HashMap<String, ClassVO>();
        for (String clsSig : clsSigs) {
            SootClass sootClass = Scene.v().getSootClass(clsSig);
            ClassVO clsVO = new ClassVO(sootClass.getName());
            clsTb.put(sootClass.getName(), clsVO);
             // add all method
            for (SootMethod sootMethod : sootClass.getMethods()) {
                    clsVO.addMethod(new MethodVO(sootMethod.getSignature(), clsVO));
            }

        }
        return clsTb;
    }
    /**
     * 获取jar包中所有不在本jar包中定义的类
     * @return
     */
    @Deprecated
    public Set<String> getPhantomClassesSigs() {
        Set<String> phantomClassesSigs = new HashSet<>();
        for (SootClass sootClass : Scene.v().getPhantomClasses()) {
            phantomClassesSigs.add(sootClass.toString());
        }
        return phantomClassesSigs;
    }
    /**
     * 该类是否有无参构造函数
     * @param sootClass
     * @return
     */
    public boolean isSimpleCls(SootClass sootClass) {
        for (SootMethod sootMethod : sootClass.getMethods()) {
            if (sootMethod.isConstructor() && sootMethod.getParameterCount() == 0) // exist constructor that doesn't
            // need param
            {
                return true;
            }
        }
        return false;
    }
    /**
     * 该类是否是Java库类
     * @param className
     * @return
     */
    public boolean isJavaLibraryClass(String className) {
        return className.startsWith("jdk.") ||
                className.startsWith("java.") ||
                className.startsWith("sun.") ||
                className.startsWith("javax.") ||
                className.startsWith("com.sun.") ||
                className.startsWith("org.omg.") ||
                className.startsWith("org.xml.") ||
                className.startsWith("org.w3c.dom");
    }
    /**
     * 获取这些jar包的调用图
     * @param classpath 所有jar包路径
     * @return
     */
    public CallGraph getCallGraph(List<String> classpath) {
        // TODO ?
        return null;
    }
    /**
     * 判断paths中是否有不以jar结尾的文件，如果有，替换后缀或删除该path
     * @param paths
     * @return
     */
    public List<String> invalidClassPreprocess(Collection<String> paths) {
        List<String> realList = new ArrayList<>();
        Iterator<String> it = paths.iterator();
        while(it.hasNext()){
            String x = it.next();
            if (new File(x).exists() && new File(x).isDirectory()) {
                realList.add(x);
                continue;
            }
            if (!new File(x).exists()) {
                continue;
            }
            if(!x.endsWith(".jar")){
                try {
                    String jar = x.substring(0, x.lastIndexOf(".")) + ".jar";
                    File jarFile = new File(jar);
                    if (jarFile.exists()) {
                        realList.add(jar);
                    }
                }
                catch (StringIndexOutOfBoundsException e) {
//                    GradleUtil.i().getLogger().error("exception classpath is: " + x);
                }
            }
            else {
                realList.add(x);
            }
        }
        return realList;
    }
    /**
     * 判断paths中是否有不以jar结尾的文件，如果有，替换后缀或删除该path
     * @param path
     * @return
     */
    public String invalidClassPreprocess(String path) {
        String x= path;
        if (new File(x).exists() && new File(x).isDirectory()) {
            return x;
        }
        if (!new File(x).exists()) {
            return x;
        }
        if (!x.endsWith(".jar")) {
            try {
                String jar = x.substring(0, x.lastIndexOf(".")) + ".jar";
                File jarFile = new File(jar);
                if (jarFile.exists()) {
                    return x;
                }
            }
            catch (StringIndexOutOfBoundsException e) {
//                GradleUtil.i().getLogger().error("exception classpath is: " + x);
            }
        } else {
            return x;
        }
        return x;
    }
}
