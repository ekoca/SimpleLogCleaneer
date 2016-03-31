package com.emrekoca.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.emrekoca.SimpleLogCleaner;

/**
 * A simple main class for running Runnables from command line.
 * 
 * @author Emre Koca
 */
public class TaskSecureKiller {
    private static Logger logger = LoggerFactory.getLogger(LogCleaner.class);

    public static void run(Class<? extends Runnable> klass) {
        // final Logger logger = Logger.getLogger(klass);
        // ClassPathXmlApplicationContext appContext = new
        // ClassPathXmlApplicationContext("application.xml");
        ApplicationContext appContext = SpringApplication.run(SimpleLogCleaner.class, new String[]{});
        try {
            logger.info("Starting " + klass.getSimpleName());
            final Runnable r = appContext.getBean(klass);

            if (r instanceof Stoppable)
                Runtime.getRuntime().addShutdownHook(new Thread("shutdown_hook") {
                    @Override
                    public void run() {
                        logger.info("Starting shutdown");
                        try {
                            ((Stoppable)r).stop();
                        } catch (Exception e) {
                            logger.error("Job crashed in shutdown process", e);
                        }
                    }
                });
            else
                SpringApplication.exit(appContext, () -> 0);

            r.run();
            logger.info("Finished");
        } catch (Throwable e) {
            logger.error("Crashed!", e);
        } finally {
            logger.info("Shutting down spring context");
            SpringApplication.exit(appContext, () -> 0);
        }
    }
}
