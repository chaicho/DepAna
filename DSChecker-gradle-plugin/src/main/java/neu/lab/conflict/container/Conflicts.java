//package neu.lab.conflict.container;
//import neu.lab.conflict.util.Conf;
//import neu.lab.conflict.vo.Conflict;
//import nju.lab.DSchecker.model.NodeAdapter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
///**
// * 描述所有jar包版本冲突的全局变量（单例类）
// */
//public class Conflicts {
//    private static Conflicts instance;
//
//    /**
//     * 用依赖树中所有结点来初始化instance（找到依赖树中所有版本冲突问题）
//     * @param nodeAdapters
//     */
//    public static void init(NodeAdapters nodeAdapters) {
//        instance = new Conflicts(nodeAdapters);
//    }
//
//    /**
//     *
//     * @return
//     */
//    public static Conflicts i() {
//        return instance;
//    }
//    private List<Conflict> container;
//    /**
//     * must initial NodeAdapters before this construct
//     *
//     */
//    private Conflicts(NodeAdapters nodeAdapters) {
//        container = new ArrayList<Conflict>();
//        for (NodeAdapter node : nodeAdapters.getAllNodeAdapter()) {
//            addNodeAdapter(node);
//        }
//        // delete conflict if there is only one version 如果只有一个版本就删除冲突
//        //如果这个方法不是需要的冲突
//        container.removeIf(conflict -> !conflict.isConflict() || !wantCal(conflict) || !wantCal2(conflict));
//    }
//    /**this method use to debug.
//     *
//     * @param conflict
//     * @return
//     */
//    private boolean wantCal(Conflict conflict) {
//        if(Conf.getInstance().callConflict==null||"".equals(Conf.getInstance().callConflict)) {
//            return true;
//        }else {
//            if(conflict.getSig().equals(Conf.getInstance().callConflict.replace("+", ":"))) {
//                return true;
//            }
//            return false;
//        }
//    }
//    /**this method use to github app.
//     *
//     * @param conflict
//     * @return boolean
//     */
//    private boolean wantCal2(Conflict conflict) {
//        if(Conf.getInstance().callConflicts == null || "".equals(Conf.getInstance().callConflicts)) {
//            return true;
//        }else {
//            List<String> list = new ArrayList<>();
//            String[] arr = Conf.getInstance().callConflicts.split(",");
//            if (arr.length == 0) {
//                list.add(Conf.getInstance().callConflicts);
//            } else {
//                list = Arrays.asList(arr);
//            }
//            return list.contains(conflict.getSig());
//        }
//    }
//    /**
//     * 获取所有的jar包版本冲突问题。
//     * @return
//     */
//    public List<Conflict> getConflicts() {
//        return container;
//    }
//    /**
//     * 如果容器中已经存在一个conflict和本nodeAdapter是相同的构件
//     * 则为这个conflict添加本节点适配器
//     * 如果容器中不存在
//     * 则本nodeAdapter作为一个conflict加入容器
//     * 然后为这个conflict加入本节点
//     * @param nodeAdapter
//     */
//    private void addNodeAdapter(NodeAdapter nodeAdapter) {
//        Conflict conflict = null;
//        for (Conflict existConflict : container) {
//            if (existConflict.sameArtifact(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId())) {
//                conflict = existConflict;
//            }
//        }
//        if (null == conflict) {
//            conflict = new Conflict(nodeAdapter.getGroupId(), nodeAdapter.getArtifactId());
//            container.add(conflict);
//        }
//        conflict.addNode(nodeAdapter);
//    }
//    /**
//     * toString，打印冲突问题
//     * @return
//     */
//    @Override
//    public String toString() {
//        String str = "project has " + container.size() + " conflict-dependency:+\n";
//        for (Conflict conflictDep : container) {
//            str = str + conflictDep.toString() + "\n";
//        }
//        return str;
//    }
//}
