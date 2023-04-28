package neu.lab.conflict.util.vo;
import java.util.HashMap;
import java.util.Map;
/**
 * ClassPackageTree的树节点
 */
public class ClassPackageTreeNode {
    String nodeName = ""; // e.g. org, ClassA
    Map<String, ClassPackageTreeNode> packageChildren = new HashMap<>();
    boolean isClass; // this node maybe pkg or class
    public ClassPackageTreeNode(String nodeName, Map<String, ClassPackageTreeNode> packageChildren) {
        this.nodeName = nodeName;
        this.packageChildren = packageChildren;
    }
    public ClassPackageTreeNode(String nodeName) {
        this.nodeName = nodeName;
    }
    public ClassPackageTreeNode() {
    }
    public ClassPackageTreeNode(String nodeName, Map<String, ClassPackageTreeNode> packageChildren, boolean isClass) {
        this.nodeName = nodeName;
        this.packageChildren = packageChildren;
        this.isClass = isClass;
    }
    public ClassPackageTreeNode(String nodeName, boolean isClass) {
        this.nodeName = nodeName;
        this.isClass = isClass;
    }
    public String getNodeName() {
        return nodeName;
    }
    public Map<String, ClassPackageTreeNode> getPackageChildren() {
        return packageChildren;
    }
    public boolean isClass() {
        return isClass;
    }
}
