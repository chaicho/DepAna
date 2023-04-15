package neu.lab.conflict.graph;

import neu.lab.conflict.vo.MethodCall;

import java.util.*;
import java.util.Map.Entry;
/**
 * 一种用于检测内核的调用图。本质上就是一种以邻接链表表示的调用图，与Graph4distance略有不同
 * @author guoruijie
 */
public class Graph4path implements IGraph{
	Map<String,Node4path> name2node;
	/**
	 * 初始化，生成邻接链表
	 * @param name2node 方法签名到这个方法一些具体信息的映射
	 * @param calls 调用图（所有边）
	 */
	public Graph4path(Map<String, Node4path> name2node, Collection<MethodCall> calls) {
		this.name2node = name2node;
		for (MethodCall call : calls) {
			addEdge(call);
		}
	}
	/**
	 * 初始化
	 * @param name2node
	 */
	public Graph4path(Map<String, Node4path> name2node) {
		this.name2node = name2node;
	}
	private void addEdge(MethodCall call) {
		name2node.get(call.getSrc()).addOutNd(call.getTgt());
//		name2node.get(call.getTgt()).addInNd(call.getSrc());
	}
	/**
	 * 获取所有Host项目（待测项目）定义的方法节点
	 * @return
	 */
	public Set<String> getHostNds() {
		Set<String> hostNds = new HashSet<String>();
		for (Node4path node : name2node.values()) {
			if (node.isHostNode()) {
				hostNds.add(node.getName());
			}
		}
		return hostNds;
	}
	/**nodes in nds2remain will be remain.
	 * @param nds2remain
	 */
	public void filterGraph(Set<String> nds2remain) {
		//delete node
		Iterator<Entry<String,Node4path>> ite = name2node.entrySet().iterator();
		while(ite.hasNext()) {
			Entry<String,Node4path> entry = ite.next();
			//node to delete.
			if(!nds2remain.contains(entry.getKey())) {
				ite.remove();
			}
		}
		//delete edge
		ite = name2node.entrySet().iterator();
		while(ite.hasNext()) {
			Entry<String,Node4path> entry = ite.next();
			entry.getValue().filterEdge(nds2remain);
		}
	}
	/**
	 * 获取节点
	 * @param nodeName 方法签名
	 * @return
	 */
	@Override
	public INode getNode(String nodeName) {
		return name2node.get(nodeName);
	}
	/**
	 * 获取所有方法节点
	 * @return
	 */
	@Override
	public Collection<String> getAllNode() {
		return name2node.keySet();
	}
	/**
	 * 直接返回邻接链表
	 * @return
	 */
	public Map<String,Node4path> getName2Node(){
		return name2node;
	}
}
