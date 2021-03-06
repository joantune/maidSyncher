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
package pt.ist.maidSyncher.domain.sync;

import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 11 de Mar de 2013
 *
 * 
 */
public interface APIObjectWrapper {

    void validateAPIObject() throws SyncEventOriginObjectChanged;

    Object getAPIObject();

}
