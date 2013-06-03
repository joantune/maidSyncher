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

import pt.ist.maidSyncher.domain.sync.SyncEvent;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 13 de Mar de 2013
 *
 * 
 */
public class SyncActionError extends RuntimeException {

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

    /**
     * @param cause
     */
    public SyncActionError(Throwable cause) {
        super(cause);
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

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public SyncActionError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
