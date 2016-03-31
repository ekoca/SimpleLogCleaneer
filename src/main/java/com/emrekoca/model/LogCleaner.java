package com.emrekoca.model;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.emrekoca.dao.BaseJdbcTemplateDAO;

@Component
public class LogCleaner extends BaseJdbcTemplateDAO implements Stoppable, Runnable {
    private static Logger logger = LoggerFactory.getLogger(LogCleaner.class);

    final AtomicBoolean isRunning = new AtomicBoolean(true);

    int getMaxExpiredRecordID() {
        // IBM DB2: select max (EVENT_ID) from ACTIVITY_LOG where CREATED_ON <
        // CURRENT_TIMESTAMP - 12 MONTHS
        return jdbcTemplate.queryForObject(
                                           "select max(EVENT_ID) from ACTIVITY_LOG where CREATED_ON < CURRENT_TIMESTAMP - INTERVAL 3 month",
                                           Integer.class);
    }

    int deleteOldRecords(int maxId) {
        return jdbcTemplate.update("delete from ACTIVITY_LOG where EVENT_ID <= ?", maxId);
    }

    /**
     * Delete old logs
     */
    public void run() {
        int maxId = getMaxExpiredRecordID();

        // check is running because the above query is slow, and stop may
        // have been issued.
        if (isRunning.get()) {
            //int deleted = deleteOldRecords(maxId);
            logger.info("Deleted " + 0 + " old log records from AVTIVITY_LOG, up to ID " + maxId);
        }
    }

    public static void main(String[] args) {
        TaskSecureKiller.run(LogCleaner.class);
    }

    @PreDestroy
    @Override
    public void stop() {
        isRunning.set(false);
    }
}
