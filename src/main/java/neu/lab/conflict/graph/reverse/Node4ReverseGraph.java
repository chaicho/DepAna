package neu.lab.conflict.graph.reverse;

import neu.lab.conflict.graph.IBook;
import neu.lab.conflict.graph.INode;
import neu.lab.conflict.graph.IRecord;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
/**
 * 方法节点，表示调用图中的一个方法，为逆调用图设计。A -> B表示B方法调用A方法
 */
public class Node4ReverseGraph implements INode {
    private String name;
    private Set<String> outs;
    /**
     * 初始化
     */
    public Node4ReverseGraph() {
        name = "";
        outs = new HashSet<>();
    }
    /**
     * 带参初始化
     * @param name 方法签名
     * @param outs 这个方法调用的方法集合
     */
    public Node4ReverseGraph(String name, Set<String> outs) {
        this.name = name;
        this.outs = outs;
    }
    /**
     * 初始化
     * @param name 方法签名
     */
    public Node4ReverseGraph(String name) {
        this.name = name;
        outs = new HashSet<>();
    }
    /**
     * 获取方法签名
     * @return
     */
    @Override
    public String getName() {
        return name;
    }
    /**
     * 获取调用本方法的所有方法
     * @return
     */
    @Override
    public Collection<String> getNexts() {
        return outs;
    }
    /**
     * 获取调用本方法的所有方法
     * @return
     */
    public Collection<String> getInNodes() {
        return outs;
    }
    /**
     * 获取方法详细说明
     * @return
     */
    @Override
    public IBook getBook() {
        return null;
    }
    /**
     * equals方法
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Node4ReverseGraph other = (Node4ReverseGraph) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
    /**
     * 生成方法被调用的record。
     * 一条record一般指的是这个方法的被调用路径等信息
     * @return
     */
    @Override
    public IRecord formNewRecord() {
        return null;
    }
    /**
     * 增加一个调用该方法的方法
     * @param tgt
     */
    public void addOutNd(String tgt) {
        outs.add(tgt);
    }
}
