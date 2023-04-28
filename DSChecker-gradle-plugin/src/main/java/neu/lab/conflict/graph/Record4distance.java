package neu.lab.conflict.graph;
/**
 * 风险方法可达性信息，包括方法签名，所在枝条，到Host项目入口方法的距离
 * @author guoruijie
 */
public class Record4distance extends IRecord {
	private String riskMthd;//riskMethod name
	private double branch;
	private double distance;
	/**
	 * 普通的初始化
	 * @param name 方法签名
	 * @param branch 枝条
	 * @param distance 如果可达，到入口方法距离
	 */
	public Record4distance(String name, double branch, double distance) {
		super();
		this.riskMthd = name;
		this.branch = branch;
		this.distance = distance;
	}
	/**
	 * 获取枝条
	 * @return
	 */
	public double getBranch() {
		return branch;
	}
	/**
	 * 获取到Host项目可达性距离
	 * @return
	 */
	public double getDistance() {
		return distance;
	}
	/**
	 * 获取方法签名
	 * @return
	 */
	public String getName() {
		return riskMthd;
	}
	/**
	 * 深拷贝
	 * @return
	 */
	@Override
	public IRecord clone() {
		return new Record4distance(riskMthd, branch, distance);
	}
	/**
	 * 换枝条
	 * @param branch2
	 */
	public void updateBranch(double branch2) {
		if (branch2 < branch) {
			branch = branch2;
		}
	}
	/**
	 * 更新距离
	 * @param distance2
	 */
	public void updateDistance(double distance2) {
		if (distance2 < distance) {
			distance = distance2;
		}
	}
	/**
	 * toString
	 * @return
	 */
	@Override
	public String toString() {
		return "Record4distance [riskMthd=" + riskMthd + ", branch=" + branch + ", distance=" + distance + "]";
	}
}
