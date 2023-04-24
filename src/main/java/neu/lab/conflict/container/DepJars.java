package neu.lab.conflict.container;

import neu.lab.conflict.util.MyLogger;
import nju.lab.DSchecker.model.DepJar;
import nju.lab.DSchecker.model.NodeAdapter;

import java.io.File;
import java.util.*;

public class DepJars {
    private static DepJars instance;
    private Set<DepJar> container;

    // sequenced container, jar classpath sequence same as the
    private List<DepJar> seqContainer;
    private Set<DepJar> usedDepJars;
    // sequenced container, sequenced used dep jars.
    private List<DepJar> seqUsedDepJars;
    private DepJar hostDepJar;

    /**
     *
     * @return
     */
    public static DepJars i() {
        return instance;
    }
    /**
     *
     * @param nodeAdapters
     * @throws Exception
     */
    public static void init(NodeAdapters nodeAdapters) throws Exception {
        if (instance == null) {
            instance = new DepJars(nodeAdapters);
        }
    }
    private DepJars(NodeAdapters nodeAdapters) throws Exception {
        container = new HashSet<>();
        seqContainer = new ArrayList<>();
//        System.out.println(nodeAdapters);
        for (NodeAdapter nodeAdapter : nodeAdapters.getAllNodeAdapter()) {
            DepJar addDepJar = new DepJar(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId(), nodeAdapter.getVersion(),
                    nodeAdapter.getClassifier(), nodeAdapter.getFilePath(),nodeAdapter.getPriority(), nodeAdapter.getDepth());
            if (!container.contains(addDepJar)) {
                container.add(addDepJar);
                seqContainer.add(addDepJar);
                addDepJar.addNodeAdapter(nodeAdapter);
                nodeAdapter.setDepJar(addDepJar);
            }
            else{
//                TODO: 2023/3/28 重复的jar
                DepJar existingDepJar = container.stream()
                        .filter(depJar -> depJar.equals(addDepJar))
                        .findFirst().
                        orElse(null);
                if (existingDepJar != null) {
                    existingDepJar.addNodeAdapter(nodeAdapter);
                    nodeAdapter.setDepJar(existingDepJar);
                }
                else{
                    MyLogger.i().warn("Empty jar in container: " + addDepJar.toString());
                }
                MyLogger.i().warn("duplicate jar: " + addDepJar.toString());
            }
        }
    }


    /**
     * 按加载优先级返回所有被加载的jar包
     * @return
     */
    public Set<DepJar> getUsedDepJars() {
        if (this.usedDepJars == null) {
            Set<DepJar> usedDepJars = new HashSet<DepJar>();
            for (DepJar depJar : container) {
                if (depJar.isSelected()) { //只要该jar没有被manage而且INCLUDED
                    usedDepJars.add(depJar); //那就把这个jar加入Set准备返回吧
                }
            }
            this.usedDepJars = usedDepJars;
        }
        return this.usedDepJars;
    }
    /**
     * 按加载优先级返回所有被加载的jar包
     * @return
     */
    public List<DepJar> getSeqUsedDepJars() {
        if (this.seqUsedDepJars == null) {
            List<DepJar> seqUsedDepJars = new ArrayList<>();
            for (DepJar depJar : seqContainer) {
                if (depJar.isSelected()) { //只要该jar没有被manage而且INCLUDED
                    seqUsedDepJars.add(depJar); //那就把这个jar加入Set准备返回吧
                }
            }
            this.seqUsedDepJars = seqUsedDepJars;
        }
        return this.seqUsedDepJars;
    }
    /**
     * get dep jar belonged to hots
     * @return DepJar hostDepJar
     */
    public DepJar getHostDepJar() {
        if (hostDepJar == null) {
            for (DepJar depJar : container) {
                if (depJar.isHost()) {
                    if (hostDepJar != null) {
                        MyLogger.i().warn("multiple depjar for host ");
                    }
                    hostDepJar = depJar;
                }
            }
            MyLogger.i().warn("depjar host is " + hostDepJar.toString()); //测试输出
        }
        return hostDepJar;
    }
    /**
     * use groupId, artifactId, version and classifier to find the same DepJar
     * @return same depJar or null
     */
    public DepJar getDep(String groupId, String artifactId, String version, String classifier) {
        for (DepJar dep : container) {
            if (dep.isSame(groupId, artifactId, version, classifier)) {
                return dep;
            }
        }
        MyLogger.i().warn("cant find dep:" + groupId + ":" + artifactId + ":" + version + ":" + classifier);
        return null;
    }
    /**
     * 获取依赖树中所有节点jar包
     * @return
     */
    public Set<DepJar> getAllDepJar() {
        return container;
    }
    /**
     * 获取所有jar包的本地路径
     * @return
     */
    public Set<String> getAllJarPaths() {
        Set<String> usedJarPaths = new HashSet<>();
        for (DepJar depJar : DepJars.i().getAllDepJar()) {
            for (String path : depJar.getJarFilePaths(true)) {
                usedJarPaths.add(path);
            }
        }
        return usedJarPaths;
    }
    /**
     * use nodeAdapter to find the same DeoJar
     * kernel is getDep(String groupId, String artifactId, String version, String classifier)
     * @return same depJar or null
     */
    public DepJar getDep(NodeAdapter nodeAdapter) {
        return getDep(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId(), nodeAdapter.getVersion(),
                nodeAdapter.getClassifier());
    }
    /**
     * 此函数存在多态
     * get all used dep jar's file path
     * @return
     */
    public List<String> getUsedJarPaths() {
        List<String> usedJarPaths = new ArrayList<String>();
        for (DepJar depJar : DepJars.i().getAllDepJar()) {
            if (depJar.isSelected()) {
                for (String path : depJar.getJarFilePaths(true)) {
                    usedJarPaths.add(path);
                }
            }
        }
        return usedJarPaths;
    }
    /**
     *
     * @param usedDepJar
     * @return
     */
    public List<String> getUsedJarPaths(DepJar usedDepJar) {
        List<String> usedJarPaths = new ArrayList<String>();
        for (DepJar depJar : DepJars.i().getAllDepJar()) {
            if (depJar.isSelected()) {
                if (depJar.isSameLib(usedDepJar)) {
                } else {
                    for (String path : depJar.getJarFilePaths(true)) {
                        usedJarPaths.add(path);
                    }
                }
            }
            for (String path : usedDepJar.getJarFilePaths(true)) {
                usedJarPaths.add(path);
            }
        }
        return usedJarPaths;
    }
    /**
     * @return path1;path2;path3
     */
    public String getUsedJarPathsStr() {
        Set<String> usedJarPath = new LinkedHashSet<String>();
        StringBuilder sb = new StringBuilder();
        for (String path : getUsedJarPaths()) {
            sb.append(path + File.pathSeparator);
        }
        String paths = sb.toString();
        paths = paths.substring(0, paths.length() - 1);// delete last ;
        return paths;
    }
    /**
     * @param cls
     * @return usedDepJar that has class.
     */
    public DepJar getClassJar(String cls) {
        for (DepJar depJar : DepJars.i().getAllDepJar()) {
            if (depJar.isSelected()) {
                if (depJar.containsCls(cls)) {
                    return depJar;
                }
            }
        }
        return null;
    }
    /**
     * 根据三坐标搜索jar包
     * @param nodeInfo
     * @return
     */
    public DepJar getDepJar(String[] nodeInfo) {
        DepJar targetDepJar = null;
        for (DepJar depJar : container) {
            if (depJar.getGroupId().equals(nodeInfo[0])
                    && depJar.getArtifactId().equals(nodeInfo[1])
                    && depJar.getVersion().equals(nodeInfo[2])) {
                targetDepJar = depJar;
                break;
            }
        }
        return targetDepJar;
    }
    /**
     * for cp arguments, add jar.
     * @param jar
     * @return
     */
    public List<String> getUsedJarPathsSeqForRisk(DepJar jar) {
        List<String> ret = new ArrayList<String>();
        for (DepJar depJar : DepJars.i().getSeqUsedDepJars()) {
            if (!depJar.isSameLib(jar)) {
                for (String path : depJar.getJarFilePaths(true)) {
                    ret.add(path);
                }
            }
        }
        ret.add(jar.getJarFilePath());
        return ret;
    }
    /**
     * when cg, depJars sequence matters. sortDepJars, make the jar relative order same as classpath
     * @param depJars
     * @return
     */
    public void sortDepJars(List<DepJar> depJars) {
        Collections.sort(depJars, (o1, o2) -> {
            if (o1.getPriority() == -1 && o2.getPriority() != -1) {
                return 1;
            }
            if (o2.getPriority() == -1 && o1.getPriority() != -1) {
                return -1;
            }
            int diff = o1.getPriority() - o2.getPriority();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        });
    }
    /**
     * 按jar包文件名字母顺序排序jar包
     * @param depJars
     */

    /**
     * 获取指定groupId, artifactId的被使用jar包
     * @param groupId
     * @param artifactId
     * @return
     */
    public DepJar getUsedDepJar(String groupId, String artifactId) {
        for (DepJar depJar : getUsedDepJars()) {
            if (depJar.getGroupId().equals(groupId) && depJar.getArtifactId().equals(artifactId)) {
                return depJar;
            }
        }
        MyLogger.i().warn("No used dep Jar for " + groupId + ":" + artifactId);
        return null;
    }
    public DepJar getSelectedDepJarById(String componentId) {
        for (DepJar depJar : container) {
            if (depJar.getName().equals(componentId) && depJar.isSelected()) {
                return depJar;
            }
        }
        return null;
    }
}




