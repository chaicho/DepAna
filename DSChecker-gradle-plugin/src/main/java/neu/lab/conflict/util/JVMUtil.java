package neu.lab.conflict.util;
/**
 * JVM监控信息输出类
 * @author guoruijie
 */
public class JVMUtil {
    private static class JVMUtilHolder {
        private static final JVMUtil INSTANCE = new JVMUtil();
    }
    private JVMUtil() {
    }
    public static JVMUtil getInstance() {
        return JVMUtilHolder.INSTANCE;
    }
    /**
     * 返回内存使用信息
     * @return
     */
    public String toMemoryInfo() {
        Runtime currRuntime = Runtime.getRuntime();
        int nFreeMemory = (int) (currRuntime.freeMemory() / 1024 / 1024);
        int nTotalMemory = (int) (currRuntime.totalMemory() / 1024 / 1024);
        return nFreeMemory + "M/" + nTotalMemory + "M(free/total)";
    }
}
