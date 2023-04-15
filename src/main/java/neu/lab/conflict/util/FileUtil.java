package neu.lab.conflict.util;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
/**
 * 文件操作工具类
 *
 * @author guoruijie
 */
public class FileUtil {
    private static class FileUtilHolder {
        private static final FileUtil INSTANCE = new FileUtil();
    }
    private FileUtil() {
    }
    public static FileUtil getInstance() {
        return FileUtilHolder.INSTANCE;
    }
    /**
     * 统计项目中java源代码行数
     *
     * @param dir
     * @return
     */
    public int countProjectLines(File dir) {
        AtomicInteger ret = new AtomicInteger(0);
        try {
            scanFile(dir, ret);
        } catch (IOException e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
        }
        return ret.get();
    }
    /**
     * 扫描项目中所有java源代码行数
     *
     * @param dir
     * @param allLines
     */
    public void scanFile(File dir, AtomicInteger allLines) throws IOException {
        // 递归查找到所有的class文件
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanFile(file, allLines);
            } else {
                if (file.getName().endsWith(".java")) {
                    allLines.addAndGet(countLines(file));
                }
            }
        }
        return;
    }
    /**
     * 统计指定文件行数
     *
     * @param file
     * @return
     */
    private int countLines(File file) throws IOException {
        int lines = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                reader.readLine();
                lines++;
            }
        } catch (IOException e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
        }
        finally {
            reader.close();
        }
        return lines;
    }
    /**
     * 创建指定文件
     *
     * @param file
     * @return
     */
    public boolean createNewFile(File file) {
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
        boolean ret = false;
        try {
            ret = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }
    /**
     * 创建临时文件
     *
     * @param prefix  前缀
     * @param suffix  后缀
     * @param content 指定要在文件中写的内容
     * @return
     */
    public static File createTempFile(String prefix, String suffix, String content) {
        File ret = null;
        try {
            ret = File.createTempFile(prefix, suffix);
            ret.deleteOnExit();
        } catch (Exception e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
        }
        if (ret == null) {
            return null;
        }
        try {
            FileUtils.writeStringToFile(ret, content);
        } catch (Exception e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
        }
        return ret;
    }
    /**
     * 将obj以json格式写入指定文件
     *
     * @param file 指定文件
     * @param obj  指定内存对象
     * @return
     */
    public boolean writeGson(File file, Object obj) {
        try {
            if (!file.exists()) {
                FileUtil.getInstance().createNewFile(file);
            }
            Writer writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(new Gson().toJson(obj));
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将指定内容写入指定文件
     *
     * @param file    指定文件
     * @param content 要写入的内容
     * @param append  是否是追加
     * @return
     */
    public boolean writeStringToFile(File file, String content, boolean append) {
        try {
            Writer writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(content);
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 判断一个.zip文件是否有效（否是损坏）
     *
     * @param file
     * @return
     */
    public boolean isZipValid(final File file) {
        ZipFile zipfile = null;
        ZipInputStream zis = null;
        try {
            zipfile = new ZipFile(file);
            zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze = zis.getNextEntry();
            if (ze == null) {
                return false;
            }
            while (ze != null) {
                // if it throws an exception fetching any of the following then we know the file is corrupted.
                zipfile.getInputStream(ze);
                ze.getCrc();
                ze.getCompressedSize();
                ze.getName();
                ze = zis.getNextEntry();
            }
            return true;
        } catch (ZipException e) {
            return false;
        } catch (IOException e) {
            System.err.println("Caught Exception!");
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
                System.err.println("Caught Exception!");
            }
            try {
                if (zis != null) {
                    zis.close();
                    zis = null;
                }
            } catch (IOException e) {
                System.err.println("Caught Exception!");
            }
        }
    }
    /**
     * 获取指定文件的SHA1 hashcode
     *
     * @param file
     * @return
     */
    public String getFileSHA1(File file) {
        MessageDigest md = null;
        FileInputStream fis = null;
        StringBuilder sha1Str = new StringBuilder();
        try {
            fis = new FileInputStream(file);
            MappedByteBuffer mbb = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            md = MessageDigest.getInstance("SHA-1");
            md.update(mbb);
            byte[] digest = md.digest();
            String shaHex = "";
            for (int i = 0; i < digest.length; i++) {
                shaHex = Integer.toHexString(digest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    sha1Str.append(0);
                }
                sha1Str.append(shaHex);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    System.err.println("Caught Exception!");
                }
            }
        }
        return sha1Str.toString();
    }
    /**
     * 移动文件到指定目录
     *
     * @param srcFileName
     * @param tgtDir
     * @return
     */
    public static boolean moveFile(String srcFileName, String tgtDir) {
        File srcFile = new File(srcFileName);
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        File destDir = new File(tgtDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return srcFile.renameTo(new File(tgtDir + File.separator + srcFile.getName()));
    }
    /**
     * 删除指定梅露露
     *
     * @param folderPath
     */
    public static void delFolder(String folderPath) {
        try {
            FileUtil.delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            System.err.println("Caught Exception!");
            e.printStackTrace();
        }
    }
    /**
     * 删除指定文件夹下所有文件
     * param path 文件夹完整绝对路径
     *
     * @param path
     * @return
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
    /**
     * 将指定文件路径的文件的HasCode写入到指定文件jarHashFilePath中
     *
     * @param jarFilePath
     * @param jarHashFilePath
     */
    public void writeJarHash(String jarFilePath, String jarHashFilePath) {
        File jarFile = new File(jarFilePath);
        // if buffer File generated later than jar file, considered as valid
        File hashFile = new File(jarHashFilePath);
        // get jar File's sha1
        String nowSHA1 = FileUtil.getInstance().getFileSHA1(jarFile);
        try {
            FileUtils.writeStringToFile(hashFile, nowSHA1);
        } catch (Exception e) {
            System.err.println("Caught Exception!");
            System.err.println("writeJarHash failed");
        }
    }
}
