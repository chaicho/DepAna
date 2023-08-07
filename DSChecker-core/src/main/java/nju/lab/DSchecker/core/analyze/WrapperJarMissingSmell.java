package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class WrapperJarMissingSmell extends BaseSmell{

    @Override
    public void detect() {
        appendToResult("========WrapperJarMissingSmell========");
        File rootPath = hostProjectInfo.getRootDir();
        String wrapperPath = hostProjectInfo.getWrapperPath();
        File gitignore = new File(rootPath.getAbsolutePath() + ".gitignore");
        System.out.println(rootPath.getAbsolutePath());
        // check if the wrapperPath is a valid directory
        File wrapperDir = new File(wrapperPath);
        if (wrapperDir.exists() && wrapperDir.isDirectory()) {
            // get all the files in the wrapperDir
            File[] files = wrapperDir.listFiles();
            if (files != null) {
                // use a boolean variable to keep track of whether any wrapper file is found or not
                boolean wrapperFound = false;
                // loop through the files and check if any of them ends with "-wrapper.jar"
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith("-wrapper.jar")) {
                        // get the name of the file without the extension
                        String name = file.getName().substring(0, file.getName().lastIndexOf("-"));
                        // output the name of the wrapper
//                        appendToResult(name + " wrapper exists in the wrapper path. This is good practice.");
                        // set the boolean variable to true
                        wrapperFound = true;
                    }
                }
                // check if the boolean variable is false
                if (!wrapperFound) {
                    // report a smell if no wrapper file is found
                    appendToResult(String.format("No wrapper jar exists in the wrapper path %s. This may cause inconsistency in the project.", wrapperDir.getAbsolutePath()));
                }
            }
        } else {
            // report a smell if the wrapperPath is not a valid directory
            appendToResult(String.format("The wrapper path %s is not a valid directory. This may cause problems in the project.", wrapperDir.getAbsolutePath()));
        }
    }
}
