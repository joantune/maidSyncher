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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 11 de Mar de 2013
 * 
 *         Used to sync the
 * 
 */
public interface SyncActionWrapper<T extends SynchableObject> {

    public Collection<T> sync() throws IOException;

    public Collection<PropertyDescriptor> getPropertyDescriptorsTicked();

    public SyncEvent getOriginatingSyncEvent();

    /**
     * 
     * @return a collection of {@link DSIObject} that must have been proccessed from the changes buzz before we can do this action
     */
    public Collection<DSIObject> getSyncDependedDSIObjects();

    public Set<Class> getSyncDependedTypesOfDSIObjects();

}
