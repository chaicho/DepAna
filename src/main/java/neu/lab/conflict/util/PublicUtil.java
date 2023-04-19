package neu.lab.conflict.util;
import neu.lab.conflict.container.DepJars;
import nju.lab.DSchecker.model.DepJar;
import java.io.File;
import java.util.Collection;
import java.util.List;
/**
 * 工具类，一些字符串处理等通用功能的提供者
 * @author guoruijie
 */
public class PublicUtil {
    private static class PublicUtilHolder {
        private static final PublicUtil INSTANCE = new PublicUtil();
    }
    private PublicUtil() {
    }
    public static PublicUtil getInstance() {
        return PublicUtilHolder.INSTANCE;
    }
    /**
     * By Grj, 为调用路径上的每个方法添加其所定义的jar包groupId:artifactId:version
     * @param mthdCallPath 调用路径，以换行符\n隔开
     * @return
     */
    public String addJarFullName(String mthdCallPath) {
        StringBuilder sb = new StringBuilder();
        String[] mthds = mthdCallPath.split("\\n");
        for (int i = 0; i < mthds.length - 1; i++) {
            // last method is risk method,don't need calculate.
            String mthd = mthds[i];
            String cls = SootUtil.getInstance().mthdSig2cls(mthd);
            DepJar depJar = DepJars.i().getClassJar(cls);
            String jarName = "";
            if (depJar != null) {
                jarName = depJar.getName() + ':' + depJar.getVersion();
            }
            sb.append(mthd + " " + jarName + ";");
        }
        sb.append(mthds[mthds.length - 1]);
        return sb.toString();
    }
    /**
     * 将字符串列表转化为以分号分隔的字符串，并为每个字符串两侧加上双引号
     * @param strs 字符串列表
     * @return "A";"B"
     */
    public String list2StrSplitColonQuote(Collection<String> strs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (String str : strs) {
            sb.append(str);
            sb.append(";");
        }
        if (sb.toString().endsWith(";")) {
            sb.deleteCharAt(sb.toString().length() - 1);
        }
        sb.append("\"");
        return sb.toString();
    }
    /**
     * 获取指定目录下所有java文件
     * @param filelist 返回值
     * @param strPath 指定目录
     */
    public void getFileList(List<File> filelist, String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(filelist, files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith(".java")) { // 判断文件名是否以.java结尾
                    filelist.add(files[i]);
                }
            }
        }
    }
}
