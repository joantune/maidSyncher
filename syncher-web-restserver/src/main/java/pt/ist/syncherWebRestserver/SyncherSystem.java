/**
 * 
 */
package pt.ist.syncherWebRestserver;

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
    public static void init() {
        SchedulerSystem schedulerSystem = SchedulerSystem.getInstance();
        if (schedulerSystem.getLoggingStorage() == null) {
            LOGGER.info("initing File Storage system for scheduler");
            LocalFileSystemStorage fileSystemStorage = new LocalFileSystemStorage("LogLFS", "/tmp/", 0);
            schedulerSystem.setLoggingStorage(fileSystemStorage);
        }

        LOGGER.info("initing Synch task");

        initSchedule();

    }

    static void initSchedule() {
        LOGGER.debug("Finding existing task");
        boolean systemAlreadyInited = false;
        for (TaskSchedule taskSchedule : SchedulerSystem.getInstance().getTaskScheduleSet()) {
            if (taskSchedule.getTaskClassName().equalsIgnoreCase(SyncherTask.class.getName())) {
                systemAlreadyInited = true;
                break;
            }
        }
        if (systemAlreadyInited == false) {
            LOGGER.info("Adding syncher task");
            new TaskSchedule(SyncherTask.class.getName(), "* * * * *");

        } else {
            LOGGER.info("Syncher task already added previously");
        }
    }

}
