package com.emrekoca.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.emrekoca.model.LogCleaner;

@Component
@EnableScheduling
public class ScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    @Autowired
    LogCleaner logCleaner;

    @Scheduled(fixedDelay = 1000)
    public void reportCurrentTime() {
        logger.info("Job started and the time is now " + dateFormat.format(new Date()));
        logCleaner.run();
    }
}
