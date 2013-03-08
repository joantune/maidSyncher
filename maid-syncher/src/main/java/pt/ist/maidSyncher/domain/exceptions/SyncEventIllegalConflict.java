/**
 * 
 */
package pt.ist.maidSyncher.domain.exceptions;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 6 de Mar de 2013
 *
 * 
 */
public class SyncEventIllegalConflict extends Error {

    public SyncEventIllegalConflict(String string) {
        super(string);
    }

}
