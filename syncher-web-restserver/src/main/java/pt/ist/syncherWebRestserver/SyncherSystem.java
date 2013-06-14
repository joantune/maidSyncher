/**
 * 
 */
package pt.ist.syncherWebRestserver;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.bennu.io.domain.LocalFileSystemStorage;
import pt.ist.bennu.scheduler.domain.SchedulerSystem;
import pt.ist.bennu.scheduler.domain.TaskSchedule;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.syncherWebRestserver.tasks.SyncherTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 24 de Mai de 2013
 * 
 * 
 */
public class SyncherSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncherSystem.class);

    @Atomic(mode = TxMode.WRITE)
    public static void init() throws IOException {
        LOGGER.info("Initing MaidSyncher SyncherSystem");
        SchedulerSystem schedulerSystem = SchedulerSystem.getInstance();
        if (schedulerSystem.getLoggingStorage() == null) {
            LOGGER.info("initing File Storage system for scheduler");
            LocalFileSystemStorage fileSystemStorage = new LocalFileSystemStorage("LogLFS", "/tmp/", 0);
            schedulerSystem.setLoggingStorage(fileSystemStorage);
        } else {
            LOGGER.info("Logging storage already existed");

        }

        LOGGER.info("initing Synch task");


    }

    @Atomic(mode = TxMode.WRITE)
    public static void initSchedule() throws IOException {
        Properties schedulerConfigurationProperties = new Properties();
        schedulerConfigurationProperties.load(SyncherSystem.class.getResourceAsStream("/configuration.properties"));
        String schedule = schedulerConfigurationProperties.getProperty("sync.task.cron.schedule");
        if (schedule == null) {
            LOGGER.warn("No schedule configuration found (on configuration.properties, property 'sync.task.cron.schedule') going with each 10 minutes");
            schedule = "*/10 * * * *";
        } else {
            LOGGER.info("Schedule of syncherTask: " + schedule);
        }
        LOGGER.debug("Finding existing task");
        boolean systemAlreadyInited = false;
        for (TaskSchedule taskSchedule : SchedulerSystem.getInstance().getTaskScheduleSet()) {
            if (taskSchedule.getTaskClassName().equalsIgnoreCase(SyncherTask.class.getName())) {
                if (taskSchedule.getSchedule().equalsIgnoreCase(schedule) == false) {
                    LOGGER.info("Found the task scheduled but with a different schedule. Previous schedule: "
                            + taskSchedule.getSchedule() + " new Schedule: " + schedule);
                    taskSchedule.delete();
                } else {
                    systemAlreadyInited = true;

                }
                break;
            }
        }
        if (systemAlreadyInited == false) {
            LOGGER.info("Adding syncher task");
            new TaskSchedule(SyncherTask.class.getName(), schedule);

        } else {
            LOGGER.info("Syncher task already added previously");
        }
    }

}
