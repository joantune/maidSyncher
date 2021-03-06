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
package pt.ist.maidSyncher.domain.github.exceptions;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 12 de Fev de 2013
 *
 * 
 */
public class OauthInvalidTokenException extends Exception {

    /**
     * 
     */
    public OauthInvalidTokenException() {
        super();
    }

    /**
     * @param message
     */
    public OauthInvalidTokenException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public OauthInvalidTokenException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public OauthInvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public OauthInvalidTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
