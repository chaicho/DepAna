package neu.lab.conflict.vo;
/**
 * 用于缓存的描述方法调用边的数据结构
 */
public class MethodCall {
	private String src;
	private String target;
	/**
	 * 调用边的源方法和目标方法，初始化
	 * @param src 源方法签名
	 * @param target 目标方法签名
	 */
	public MethodCall(String src, String target) {
		this.src = src;
		this.target = target;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodCall) {
			MethodCall rlt = (MethodCall) obj;
			return src.equals(rlt.getSrc()) && target.equals(rlt.getTgt());
		} else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		return src.hashCode() * 31 + target.hashCode();
	}
	public String getSrc() {
		return src;
	}
	public String getTgt() {
		return target;
	}
	@Override
	public String toString() {
		return src + " to " + target;
	}
}
