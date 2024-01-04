package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WrapperJarAbnormalSmell extends BaseSmell{
    public static String calculateSHA256Hash(String filePath) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");

            // Open the file for reading
            FileInputStream fileInputStream = new FileInputStream(filePath);

            // Buffer to read the file in chunks
            byte[] buffer = new byte[8192];
            int bytesRead;

            // Update the MessageDigest with file content
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                sha256Digest.update(buffer, 0, bytesRead);
            }

            // Close the input stream
            fileInputStream.close();

            // Get the hash value as bytes
            byte[] hashBytes = sha256Digest.digest();

            // Convert bytes to hexadecimal format
            StringBuilder hashHex = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hashHex.append(String.format("%02x", hashByte));
            }

            return hashHex.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void detect() {
        appendToResult("========WrapperJarAbnormalSmell========");
        String wrapperJarPath = hostProjectInfo.getWrapperPath() + File.separator +  hostProjectInfo.getBuildTool() + "-wrapper.jar";
        String wrapperPropertiesPath = hostProjectInfo.getWrapperPath() + File.separator +  hostProjectInfo.getBuildTool() + "-wrapper.properties";
        String wrapperVersion = null;
        Pattern versionPattern = null;

        if (new File(wrapperPropertiesPath).exists()){
            try {
                // Read the file
                BufferedReader reader = new BufferedReader(new FileReader(wrapperPropertiesPath));
                String line;
                // Regular expression to match Maven version
                if (hostProjectInfo.getBuildTool().equals("gradle")) {
                    versionPattern = Pattern.compile("distributionUrl=.*?gradle-([\\d.]+)-.*?.zip");
                }
                else if (hostProjectInfo.getBuildTool().equals("maven")) {
                    versionPattern = Pattern.compile("wrapperUrl=.*?/([\\d.]+)/maven-wrapper-[\\d.]+.jar");
                }
                // Iterate through lines and look for version
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = versionPattern.matcher(line);
                    if (matcher.find()) {
                        wrapperVersion = matcher.group(1);
                        break;
                    }
                }
                log.info("Wrapper Version: " + wrapperVersion);
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (new File(wrapperJarPath).exists() && wrapperVersion != null) {
            try {
                // Load the JSON resource from the classpath
                InputStream resourceStream = WrapperJarAbnormalSmell.class.getClassLoader().getResourceAsStream(String.format("wrapperData/%sWrapperChecksum.json", hostProjectInfo.getBuildTool()));
                if (resourceStream != null) {
                    JSONTokener tokener = new JSONTokener(resourceStream);
                    
                    // Create a JSON-like map object from JSON data
                    JSONObject jsonMap = new JSONObject(tokener);
                    // Get the checksum of the wrapper jar
                    String expected_checksum = jsonMap.getJSONObject(wrapperVersion).getString("wrapper_jar_checksum");
                    String actual_checksum = calculateSHA256Hash(wrapperJarPath);
                    if (!actual_checksum.equals(expected_checksum)) {
                        log.warn("Wrapper Jar Abnormal Smell: " + wrapperJarPath);
                        appendToResult("Wrapper Jar Abnormal Smell: " + wrapperJarPath);
                        appendToResult(String.format("Expected checksum for version %s :", wrapperVersion ) + expected_checksum);
                        appendToResult("Actual checksum: " + actual_checksum);
                    }
                } else {
                    log.error("JSON resource not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }
    public static void main(String[] args) {


    }
}

