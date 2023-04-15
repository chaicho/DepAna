package neu.lab.conflict.graph;
/**
 * 一种描述可达性分析结果的数据结构
 *
 * @author guoruijie
 */
public class Record4path extends IRecord {
	private String pathStr;
	private int pathlen;
	private String riskMthd;
	private String headMthd;
	/**
	 * 初始化
	 * @param riskMthd 风险方法签名
	 * @param pathStr 以换行符分隔的从Host项目到该风险方法的路径
	 * @param pathlen 路径长度
	 */
	public Record4path(String riskMthd, String pathStr, int pathlen) {
		super();
		this.riskMthd = riskMthd;
		this.pathStr = pathStr;
		this.pathlen = pathlen;
	}
	/**
	 * 初始化
	 * @param riskMthd 风险方法签名
	 * @param pathStr 以换行符分隔的从Host项目到该风险方法的路径
	 * @param pathlen 路径长度
	 * @param headMthd 入口方法
	 */
	public Record4path(String riskMthd, String pathStr, int pathlen, String headMthd) {
		super();
		this.riskMthd = riskMthd;
		this.pathStr = pathStr;
		this.pathlen = pathlen;
		this.headMthd = headMthd;
	}
	/**
	 * 获取入口方法
	 * @return
	 */
	public String getHeadMthd() {
		return headMthd;
	}
	/**
	 * 获取描述路径的Str（路径各节点以换行符隔开）
	 * @return
	 */
	public String getPathStr() {
		return pathStr;
	}
	/**
	 * 获取Host项目调用到该风险方法的调用路径长度
	 * @return
	 */
	public int getPathlen() {
		return pathlen;
	}
	/**
	 * 深拷贝
	 * @return
	 */
	@Override
	public IRecord clone() {
		return null;
	}
	/**
	 * set
	 * @param pathStr 以换行符分隔的从Host项目到该风险方法的路径
	 */
	public void setPathStr(String pathStr) {
		this.pathStr = pathStr;
	}
	/**
	 * set
	 * @param pathlen 路径长度
	 */
	public void setPathlen(int pathlen) {
		this.pathlen = pathlen;
	}
	/**
	 * 返回该风险方法签名
	 * @return
	 */
	public String getRiskMthd() {
		return riskMthd;
	}
	/**
	 * toString
	 * @return
	 */
	@Override
	public String toString() {
		return "record:" + System.lineSeparator() + pathStr;
	}
	//	@Override
	//	public void finalize() {
	//		System.out.println("release one record");
	//	}
}
