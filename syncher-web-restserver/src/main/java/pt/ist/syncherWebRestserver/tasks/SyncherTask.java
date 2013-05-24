/**
 * 
 */
package pt.ist.syncherWebRestserver.tasks;

import pt.ist.bennu.scheduler.CronTask;
import pt.ist.bennu.scheduler.annotation.Task;
import pt.ist.syncherWebRestserver.rest.SyncLogResource;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 24 de Mai de 2013
 *
 * 
 */
@Task(englishTitle = "Syncher task")
public class SyncherTask extends CronTask {


    /* (non-Javadoc)
     * @see pt.ist.bennu.scheduler.CronTask#runTask()
     */
    @Override
    public void runTask() {
        SyncLogResource.counter++;
        System.out.println("COUNTER TEST");
    }

}
