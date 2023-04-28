package neu.lab.conflict.graph;
import java.util.Collection;
/**
 * 用于后续内核检测的邻接链表数据结构，Graph4distance和Graph4path都继承于它
 * @author SUNJUNYAN
 */
public interface IGraph {
	public INode getNode(String nodeName);
	public Collection<String> getAllNode();
}
