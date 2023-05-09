package nju.lab.DSchecker.core.analyze;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class WrapperSmell extends BaseSmell{

    @Override
    public void detect() {
        output("=======WrapperSmell=======");
        File rootPath = hostProjectInfo.getRootDir();
        String wrapperPath = hostProjectInfo.getWrapperPath();
        File gitignore = new File(rootPath.getAbsolutePath() + ".gitignore");
        // check if the gitignore file exists and is readable
        if (gitignore.exists() && gitignore.canRead()) {
            try {
                // read all the lines from the gitignore file
                List<String> lines = Files.readAllLines(gitignore.toPath());
                // filter the lines that contain "-wrapper.jar" and collect them into a list
                List<String> wrapperLines = lines.stream()
                        .filter(line -> line.contains("-wrapper.jar"))
                        .collect(Collectors.toList());
                // check if the list is empty or not
                if (!wrapperLines.isEmpty()) {
                    // Wrapper jar is ignored, report a smell
                    log.warn("Wrapper jar is ignored in the .gitignore file. This may cause conflict in the build process.");
                    output("Wrapper jar is ignored in the .gitignore file. This may cause conflict in the build process.");
                }
            } catch (IOException e) {
                // handle the exception
                log.error("Failed to read the .gitignore file.", e);
            }
        } else {
            // report a smell if the gitignore file does not exist or is not readable
            output("The .gitignore file does not exist or is not readable. This may cause unwanted files to be tracked by version control.");
        }

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
                        output(name + " wrapper exists in the wrapper path. This is good practice.");
                        // set the boolean variable to true
                        wrapperFound = true;
                    }
                }
                // check if the boolean variable is false
                if (!wrapperFound) {
                    // report a smell if no wrapper file is found
                    output("No wrapper file exists in the wrapper path. This may cause inconsistency in the project.");
                }
            }
        } else {
            // report a smell if the wrapperPath is not a valid directory
            output("The wrapper path is not a valid directory. This may cause problems in the project.");
        }
    }
}
