package neu.lab.conflict.graph;
/**
 * 一条记录，描述Host项目到达某个方法的调用路径等信息
 * @author guoruijie
 */
public abstract class IRecord {
	@Override
	public abstract IRecord clone();
}
