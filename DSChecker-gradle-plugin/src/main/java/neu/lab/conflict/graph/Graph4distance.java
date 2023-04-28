package neu.lab.conflict.graph;

import neu.lab.conflict.vo.MethodCall;

import java.util.*;
/**
 * 一种用于检测内核的调用图。本质上就是一种以邻接链表表示的调用图
 * @author guoruijie
 */
public class Graph4distance implements IGraph {
	Map<String, Node4distance> name2node;
	/**
	 * 初始化，生成邻接链表
	 * @param name2node 方法签名到这个方法一些具体信息的映射
	 * @param calls 调用图（所有边）
	 */
	public Graph4distance(Map<String, Node4distance> name2node, Collection<MethodCall> calls) {
		this.name2node = name2node;
		for (MethodCall call : calls) {
			addEdge(call);
		}
	}
	/**
	 * 返回邻接链表
	 * @return
	 */
	public Map<String, Node4distance> getName2node() {
		return name2node;
	}
	/**
	 * 通过本数据结构生成另一种用于内核可达性分析计算的调用图数据结构
	 * @return
	 */
	public Graph4path getGraph4path() {
		Map<String, Node4path> name2pathNode = new HashMap<>(0);
		//add node
		for (String nd2remain : name2node.keySet()) {
			Node4distance distanceNode = name2node.get(nd2remain);
			name2pathNode.put(nd2remain, new Node4path(nd2remain, distanceNode.isHostNode(), distanceNode.isRisk()));
		}
		//add relation
		for (String nd2remain : name2node.keySet()) {
			Node4distance distanceNode = name2node.get(nd2remain);
			for(String out:distanceNode.getNexts()) {
					name2pathNode.get(nd2remain).addOutNd(out);
			}
		}
		return new Graph4path(name2pathNode);
	}
	/**
	 * 通过本数据结构生成另一种用于内核可达性分析计算的调用图数据结构
	 * @return
	 */
	public Graph4path getGraph4path(Set<String> nds2remain) {
		Map<String, Node4path> name2pathNode = new HashMap<>(0);
		//add node
		for (String nd2remain : nds2remain) {
			Node4distance distanceNode = name2node.get(nd2remain);
			name2pathNode.put(nd2remain, new Node4path(nd2remain, distanceNode.isHostNode(), distanceNode.isRisk()));
		}
		//add relation
		for (String nd2remain : nds2remain) {
			Node4distance distanceNode = name2node.get(nd2remain);
			for(String out:distanceNode.getNexts()) {
				if(nds2remain.contains(out)) {
					name2pathNode.get(nd2remain).addOutNd(out);
				}
			}
		}
		return new Graph4path(name2pathNode);
	}
	/**
	 * 获取所有Host项目（待测项目）定义的方法节点
	 * @return
	 */
	public Set<String> getHostNds() {
		Set<String> hostNds = new HashSet<String>();
		for (Node4distance node : name2node.values()) {
			if (node.isHostNode()) {
				hostNds.add(node.getName());
			}
		}
		return hostNds;
	}
	private void addEdge(MethodCall call) {
		name2node.get(call.getSrc()).addOutNd(call.getTgt());
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
}
