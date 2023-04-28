package neu.lab.conflict.util.vo;
import java.util.Collection;
import java.util.List;
/**
 * sootExcludePkg优化树。这个树的作用在于，找到exclude最小前缀集合
 * 例如java.io.*, com.*...
 */
public class ClassPackageTree {
    ClassPackageTreeNode root;
    /**
     * 初始化
     */
    public ClassPackageTree() {
        root = new ClassPackageTreeNode();
    }
    /**
     * 初始化
     * @param classes
     */
    public ClassPackageTree(Collection<String> classes) {
        root = new ClassPackageTreeNode();
        for (String cls : classes) {
            addClass(cls);
        }
    }
    private void addClass(String className) {
        String[] classNameSplit = className.split("\\.|\\s+");
        ClassPackageTreeNode node = root;
        for (int i = 0; i < classNameSplit.length; ++i) {
            if (!node.packageChildren.containsKey(classNameSplit[i])) {
                ClassPackageTreeNode child = null;
                if (i == classNameSplit.length - 1) {
                    child = new ClassPackageTreeNode(/*node.nodeName + "." + */classNameSplit[i], true);
                }
                else {
                    child = new ClassPackageTreeNode(classNameSplit[i], false);
                }
                node.packageChildren.put(classNameSplit[i], child);
            }
            node = node.packageChildren.get(/*node.nodeName + "." +*/ classNameSplit[i]);
        }
    }
    /**
     * get
     * @return
     */
    public ClassPackageTreeNode getRoot() {
        return root;
    }
    /**
     * 树种是否有某个前缀，如java.io.*
     * @param classPackageSplit
     * @return
     */
    @Deprecated
    public boolean containsPathString(List<String> classPackageSplit) {
        if (classPackageSplit.size() == 0) {
            return true;
        }
        ClassPackageTreeNode node = root;
        for (int i = 0; i < classPackageSplit.size(); ++i) {
            if (!node.packageChildren.containsKey(classPackageSplit.get(i))) {
                return false;
            }
            node = node.packageChildren.get(classPackageSplit.get(i));
        }
        return false;
    }
    /**
     * path need to start from root!!!
     * @param classPackageSplit
     * @return
     */
    public boolean containsPathNode(List<ClassPackageTreeNode> classPackageSplit) {
        if (classPackageSplit.size() == 0) {
            return true;
        }
        if (!"".equals(classPackageSplit.get(0).getNodeName())) {
            return false;
        }
        ClassPackageTreeNode node = root;
        for (int i = 1; i < classPackageSplit.size(); ++i) {
            if (!node.packageChildren.containsKey(classPackageSplit.get(i).getNodeName())) {
                return false;
            }
            node = node.packageChildren.get(classPackageSplit.get(i).getNodeName());
        }
        return true;
    }
}
