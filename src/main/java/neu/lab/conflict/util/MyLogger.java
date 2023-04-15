package neu.lab.conflict.util;

import org.gradle.api.logging.Logger;

public class MyLogger {

    private static MyLogger instance = null;
    private Logger logger;

    private MyLogger() {
        logger = null;
    }

    public static void init(Logger taskLogger){
        if(instance == null){
            instance = new MyLogger();
        }
        instance.logger = taskLogger;
    }

    public static synchronized Logger i() {
        return instance.logger;
    }

    public void log(String message) {
        logger.info(message);
    }
}

