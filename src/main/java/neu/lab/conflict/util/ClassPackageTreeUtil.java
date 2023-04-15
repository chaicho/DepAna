package neu.lab.conflict.util;

import neu.lab.conflict.util.vo.ClassPackageTree;
import neu.lab.conflict.util.vo.ClassPackageTreeNode;

import java.util.*;

/**
 * sootExcludePkg优化工具类
 */
public class ClassPackageTreeUtil {
    private static class ClassPackageTreeUtilHolder {
        private static final ClassPackageTreeUtil INSTANCE = new ClassPackageTreeUtil();
    }
    private ClassPackageTreeUtil() {
    }
    public static ClassPackageTreeUtil getInstance() {
        return ClassPackageTreeUtilHolder.INSTANCE;
    }
    /**
     * 获取需要排除的最简前缀集合
     * @param allClses 全体类
     * @param includeClses 需要分析的类
     * @return [java.io.*, com.google.guava.*]
     */
    public Collection<String> getMinimumExcludePkgs(Collection<String> allClses, Collection<String> includeClses) {
        Collection<String> ret = new ArrayList<>();
        ClassPackageTree classPackageTree = new ClassPackageTree(allClses);//构建所有类的目录树
        ClassPackageTree includeClassPackageTree = new ClassPackageTree(includeClses);//构建需要分析类的目录树
        Queue<List<ClassPackageTreeNode>> workList = new LinkedList<>();
        List<ClassPackageTreeNode> rootPath = new ArrayList<>();
        rootPath.add(classPackageTree.getRoot());
        workList.add(rootPath);
        while (!workList.isEmpty()) {
            List<ClassPackageTreeNode> currentPath = workList.poll();
            if (includeClassPackageTree.containsPathNode(currentPath)) {
                ClassPackageTreeNode pathEndNode = currentPath.get(currentPath.size() - 1);
                for (ClassPackageTreeNode value : pathEndNode.getPackageChildren().values()) {
                    List<ClassPackageTreeNode> newPath = new ArrayList<>(currentPath);
                    newPath.add(value);
                    workList.add(newPath);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (ClassPackageTreeNode pathNode : currentPath) {
                    if ("".equals(pathNode.getNodeName())) {
                        continue;
                    }
                    sb.append(pathNode.getNodeName());
                    if (!pathNode.isClass()) {
                        sb.append(".");
                    }
                }
                String excludeResult = sb.toString();
                if (excludeResult.endsWith(".")) {
                    excludeResult += "*";
                    ret.add(excludeResult);
                }
            }
        }
        return ret;
    }
}
