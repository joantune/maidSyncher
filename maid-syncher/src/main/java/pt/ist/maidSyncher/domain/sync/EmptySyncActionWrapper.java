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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.exceptions.SyncActionError;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 13 de Mar de 2013
 * 
 *         An empty sync action wrapper - i.e. that leads to no sync action
 */
public class EmptySyncActionWrapper implements SyncActionWrapper {

    private final SyncEvent syncEvent;
    /**
     * 
     */
    public EmptySyncActionWrapper(final SyncEvent originalSyncEvent) {
        this.syncEvent = originalSyncEvent;
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.domain.sync.SyncActionWrapper#sync()
     */
    @Override
    public Set<SynchableObject> sync() throws SyncActionError {
        return Collections.emptySet();
    }

    @Override
    public Collection getPropertyDescriptorNamesTicked() {
        return syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList();
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.domain.sync.SyncActionWrapper#getOriginatingSyncEvent()
     */
    @Override
    public SyncEvent getOriginatingSyncEvent() {
        return syncEvent;
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.domain.sync.SyncActionWrapper#getSyncDependedDSIObjects()
     */
    @Override
    public Collection<DSIObject> getSyncDependedDSIObjects() {
        return Collections.emptyList();
    }

    @Override
    public Set getSyncDependedTypesOfDSIObjects() {
        return Collections.emptySet();
    }

}
