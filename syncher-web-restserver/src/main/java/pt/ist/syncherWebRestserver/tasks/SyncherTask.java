/**
 * 
 */
package pt.ist.syncherWebRestserver.tasks;

import pt.ist.Main;
import pt.ist.bennu.scheduler.CronTask;
import pt.ist.bennu.scheduler.annotation.Task;
import pt.ist.fenixframework.core.WriteOnReadError;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 24 de Mai de 2013
 * 
 * 
 */
@Task(englishTitle = "Syncher task")
public class SyncherTask extends CronTask {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SyncherTask.class);

    /* (non-Javadoc)
     * @see pt.ist.bennu.scheduler.CronTask#runTask()
     */
    @Override
    public void runTask() {
        getLogger().info("Running syncher - triggered by scheduler");
        taskLog("Running syncher - triggered by scheduler");
        try {
            Main.main(new String[] {});
        } catch (Throwable e) {
            if (e.getCause() instanceof WriteOnReadError) { //it shouldn't happen
                throw (WriteOnReadError) e.getCause();
            }
            if (e instanceof WriteOnReadError)
                throw (WriteOnReadError) e;
            getLogger().info("Running syncher - Caught exception", e);
            taskLog("Running syncher - Caught exception", e);
        }
    }

}
