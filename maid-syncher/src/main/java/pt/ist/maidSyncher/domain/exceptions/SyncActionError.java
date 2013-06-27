/*******************************************************************************
 * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Luis Silva - ACGHSync
 *     João Antunes - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package pt.ist.maidSyncher.domain.exceptions;

import java.util.Set;

import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 13 de Mar de 2013
 *
 * 
 */
public class SyncActionError extends RuntimeException {

    private Set<SynchableObject> changedObjects;

    /**
     * 
     */
    public SyncActionError() {
        super();
    }

    /**
     * @param message
     */
    public SyncActionError(String message) {
        super(message);
    }

    public SyncActionError(String message, Set<SynchableObject> synchedObjectsSoFar) {
        super(message);
        this.changedObjects = synchedObjectsSoFar;
    }

    /**
     * @param cause
     */
    public SyncActionError(Throwable cause) {
        super(cause);
    }

    public SyncActionError(Throwable cause, Set<SynchableObject> changedObjects) {
        super(cause);
        this.changedObjects = changedObjects;
    }

    /**
     * @param message
     * @param cause
     */
    public SyncActionError(String message, SyncEvent syncEvent, Throwable cause) {
        super(message + " SyncEvent: " + syncEvent.toString(), cause);
    }

    /**
     * @param message
     * @param cause
     */
    public SyncActionError(String message, Throwable cause) {
        super(message, cause);
    }

    public SyncActionError(String message, Throwable cause, Set<SynchableObject> changedObjects) {
        super(message, cause);
        this.changedObjects = changedObjects;
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public SyncActionError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Set<SynchableObject> getChangedObjects() {
        return changedObjects;
    }

}
