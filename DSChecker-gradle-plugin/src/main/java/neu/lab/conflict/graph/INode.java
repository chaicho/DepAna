package neu.lab.conflict.graph;
import java.util.Collection;
/**
 * 用于表征方法的节点数据结构。比如需要描述这个方法的签名、出边、Host项目可达性信息等等
 *
 * @author guoruijie
 */
public interface INode {
	public String getName();
	public Collection<String> getNexts();//next nodes that dog should go when writes book about this node.
	public IBook getBook();
	//if this node is a end node,node should form a new record.Else nodes change the copy of end node.
	//call by afterAddAllChildren.
	public IRecord formNewRecord();
}
