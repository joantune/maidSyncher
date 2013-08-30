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
import pt.ist.maidSyncher.domain.MaidRoot;
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

    static Properties schedulerConfigurationProperties = new Properties();

    {
        try {
            schedulerConfigurationProperties.load(SyncherSystem.class.getResourceAsStream("/configuration.properties"));
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public static String getSchedule() {
        String schedule = schedulerConfigurationProperties.getProperty("sync.task.cron.schedule");

        if (schedule == null) {
            LOGGER.warn("No schedule configuration found (on configuration.properties, property 'sync.task.cron.schedule') going with each 10 minutes");
            schedule = "*/10 * * * *";
        } else {
            LOGGER.info("Schedule of syncherTask: " + schedule);
        }
        return schedule;
    }

    @Atomic(mode = TxMode.WRITE)
    public static void initSchedule() throws IOException {
        //String shouldDisableScheduleString = schedulerConfigurationProperties.getProperty("sync.task.cron.disable");
        //Boolean shouldDisableSchedule =
        //       shouldDisableScheduleString == null ? false : Boolean.valueOf(shouldDisableScheduleString);
        final Boolean shouldDisableSchedule = !MaidRoot.getInstance().getRunScheduler();

        LOGGER.debug("Finding existing task");
        boolean systemAlreadyInited = false;
        for (TaskSchedule taskSchedule : SchedulerSystem.getInstance().getTaskScheduleSet()) {
            if (taskSchedule.getTaskClassName().equalsIgnoreCase(SyncherTask.class.getName())) {
                if (shouldDisableSchedule) {
                    taskSchedule.delete();
                } else {
                    if (taskSchedule.getSchedule().equalsIgnoreCase(getSchedule()) == false) {
                        LOGGER.info("Found the task scheduled but with a different schedule. Previous schedule: "
                                + taskSchedule.getSchedule() + " new Schedule: " + getSchedule());
                        taskSchedule.delete();
                    } else {
                        systemAlreadyInited = true;

                    }
                    break;
                }
            }
        }
        if (systemAlreadyInited == false && !shouldDisableSchedule) {
            LOGGER.info("Adding syncher task");
            new TaskSchedule(SyncherTask.class.getName(), getSchedule());

        } else {
            LOGGER.info("Syncher task already added previously");
        }
        //SchedulerSystem.queue(taskRunner)
        //SchedulerSystem.getInstance().getTaskScheduleSet().iterator().next().getTaskRunner()

    }

}
