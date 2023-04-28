package neu.lab.conflict.graph.reverse;

import neu.lab.conflict.graph.Graph4path;
import neu.lab.conflict.graph.IGraph;
import neu.lab.conflict.graph.INode;
import neu.lab.conflict.graph.Node4path;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.util.vo.RefedMthdDetail;
import neu.lab.conflict.vo.GsonEdgeVO;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;
/**
 * 为调用图缓存所设计，逆调用图。A -> {B,C,D}表示B,C,D调用A方法
 */
@Deprecated
public class ReverseGraph4Reachability implements IGraph {
    Map<String, Node4ReverseGraph> name2node; //入边表
    /**
     * 通过调用图，构建逆调用图
     * @param calls 调用图
     */
    public ReverseGraph4Reachability(Collection<GsonEdgeVO> calls) {
        name2node = new HashMap<>();
        for (GsonEdgeVO call : calls) {
            addEdge(call);
        }
    }
    /**
     * 通过调用图，构建逆调用图
     * @param cg 调用图
     */
    public ReverseGraph4Reachability(CallGraph cg) {
        name2node = new HashMap<>();
        for (Edge edge : cg) {
            addEdge(edge);
        }
    }
    /**
     * 通过方法签名获取指定节点
     * @param nodeName 方法签名
     * @return
     */
    @Override
    public INode getNode(String nodeName) {
        return name2node.get(nodeName);
    }
    /**
     * 获取逆调用图中的所有节点
     * @return
     */
    @Override
    public Collection<String> getAllNode() {
        return name2node.keySet();
    }
    /**
     * 根据一条调用关系call (A->B)，新增逆调用图边B->A
     * @param call
     */
    private void addEdge(GsonEdgeVO call) {
        if (!name2node.containsKey(call.getTgtMthdSig())) {
            Node4ReverseGraph node = new Node4ReverseGraph(call.getTgtMthdSig());
            name2node.put(call.getTgtMthdSig(), node);
        }
        name2node.get(call.getTgtMthdSig()).addOutNd(call.getSrcMthdSig());
    }
    private void addEdge(Edge call) {
        if (!name2node.containsKey(call.tgt().getSignature())) {
            Node4ReverseGraph node = new Node4ReverseGraph(call.tgt().getSignature());
            name2node.put(call.tgt().getSignature(), node);
        }
        name2node.get(call.tgt().getSignature()).addOutNd(call.src().getSignature());
    }
    /**
     * 搜索所有可能调用到entryMthds的所有方法
     * @param entryMthds 被调用方法集合
     * @return 可达entryMthds的所有方法
     */
    public Set<String> getReverseReachableMethods(Set<String> entryMthds) {
        Set<String> ret = new HashSet<>();//Reverse Reachable Methods
        for (String entryMthd : entryMthds) {
            ret.add(entryMthd);
            Queue<String> q = new LinkedList<>();
            q.offer(entryMthd);
            while(!q.isEmpty())
            {
                String nodeStr = q.poll();
                Node4ReverseGraph node = name2node.get(nodeStr);
                if (node == null) {
                    continue;
                }
                for(String inNode : node.getNexts())
                {
                    if(!ret.contains(inNode))
                    {
                        ret.add(inNode);
                        q.offer(inNode);
                    }
                }
            }
        }
        return ret;
    }
    /**
     * 获取Graph4path，一种用于可达性分析的数据结构
     * @return
     */
    public Graph4path getGraph4path() {
        Map<String, Node4path> name2pathNode = new HashMap<String, Node4path>();
        //add node
        for (String nd2remain : name2node.keySet()) {
            Node4ReverseGraph reverseNode = name2node.get(nd2remain);
            name2pathNode.put(nd2remain, new Node4path(nd2remain, false, true));
        }
        //add relation
        for (String nd2remain : name2node.keySet()) {
            Node4ReverseGraph reverseNode = name2node.get(nd2remain);
            for(String out:reverseNode.getNexts()) {
                name2pathNode.get(nd2remain).addOutNd(out);
            }
        }
        return new Graph4path(name2pathNode);
    }
    /**
     * 获取Graph4path，一种用于可达性分析的数据结构
     * @return
     */
    public Graph4path getGraph4path(Map<String, RefedMthdDetail> hostRefedMthds) {
        Map<String, Node4path> name2pathNode = new HashMap<String, Node4path>();
        //System.out.println(hostRefedMthds.values());
        Set<String> hostRefedMthdsClses = new HashSet<>();
        //for ()
        //add node
        for (String nd2remain : name2node.keySet()) {
            String ndCls = SootUtil.getInstance().mthdSig2cls(nd2remain);
            String ndOnlyName = SootUtil.getInstance().mthdSig2Onlyname(nd2remain);
            Node4ReverseGraph reverseNode = name2node.get(nd2remain);
            //System.out.println("Host?" + hostRefedMthds.values().contains(ndOnlyName) + ndOnlyName);
            name2pathNode.put(nd2remain, new Node4path(nd2remain, false,
                    hostRefedMthds.values().contains(ndOnlyName) &&
                            hostRefedMthds.get(ndOnlyName).getInternalTypeName().equals(ndCls)));
        }
        //add relation
        for (String nd2remain : name2node.keySet()) {
            Node4ReverseGraph reverseNode = name2node.get(nd2remain);
            for(String out:reverseNode.getNexts()) {
                name2pathNode.get(nd2remain).addOutNd(out);
            }
        }
        return new Graph4path(name2pathNode);
    }
    /**
     * 判断本方法是否属于Host节点
     * @return
     */
    @Deprecated
    public boolean isHostNode() {
        return false;
    }
}
