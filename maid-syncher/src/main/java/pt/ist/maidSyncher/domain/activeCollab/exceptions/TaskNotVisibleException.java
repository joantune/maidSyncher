/**
 * 
 */
package pt.ist.maidSyncher.domain.activeCollab.exceptions;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 26 de Fev de 2013
 * 
 *         If a task is not visible, when it is proccessed, a TaskNotVisibleException is thrown
 */
public class TaskNotVisibleException extends Error {

    public TaskNotVisibleException(IllegalStateException ex) {
        super(ex);
    }

    public TaskNotVisibleException() {
        super();
    }

}
