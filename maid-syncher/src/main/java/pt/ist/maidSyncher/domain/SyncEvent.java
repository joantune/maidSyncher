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
package pt.ist.maidSyncher.domain;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalTime;

import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 4 de Mar de 2013
 *
 * 
 */
public class SyncEvent {

    public static enum TypeOfChangeEvent {
        CREATE, READ, UPDATE, DELETE;
    }

    public static enum SyncUniverse {
        ACTIVE_COLLAB, GITHUB;
    }

    /**
     * The instant of then the change occurred (it should be read from the GH/AC api object, field either created on or updated
     * at)
     */
    private final LocalTime dateOfChange;

    private final TypeOfChangeEvent typeOfChangeEvent;

    private final Set<PropertyDescriptor> changedPropertyDescriptors;

    private final DSIObject dsiElement;

    private final APIObjectWrapper apiObjectWrapper;

    private final SyncUniverse targetSyncUniverse;

//    private final SynchableObject targetObject;

    private final SynchableObject originObject;

    public SyncEvent(LocalTime dateOfChange, TypeOfChangeEvent changeEvent, Collection<PropertyDescriptor> propertyDescriptors,
            DSIObject dsiObject, APIObjectWrapper apiObjectWrapper, SyncUniverse targetSyncUniverse,
            SynchableObject origin) {
        this.dateOfChange = dateOfChange;
        this.typeOfChangeEvent = changeEvent;
        this.changedPropertyDescriptors = new HashSet<PropertyDescriptor>(propertyDescriptors);
        this.dsiElement = dsiObject;
        this.apiObjectWrapper = apiObjectWrapper;
        this.targetSyncUniverse = targetSyncUniverse;
        this.originObject = origin;

    }

    public LocalTime getDateOfChange() {
        return dateOfChange;
    }

    public TypeOfChangeEvent getTypeOfChangeEvent() {
        return typeOfChangeEvent;
    }

    public Set<PropertyDescriptor> getChangedPropertyDescriptors() {
        return changedPropertyDescriptors;
    }

    public DSIObject getDsiElement() {
        return dsiElement;
    }

//    public SynchableObject getTargetObject() {
//        return targetObject;
//    }

    public SynchableObject getOriginObject() {
        return originObject;
    }

    public SyncUniverse getTargetSyncUniverse() {
        return targetSyncUniverse;
    }

    @Override
    public String toString() {
        return "Sync event, DSIElement: " + getDsiElement().getExternalId() + " (" + getDsiElement().getClass().getSimpleName()
                + ")" + " Type: " + getTypeOfChangeEvent() + " targetUniverse: " + getTargetSyncUniverse() + " originObject: "
                + getOriginObject().getClass().getSimpleName();
    }

    public APIObjectWrapper getApiObjectWrapper() {
        return apiObjectWrapper;
    }

}
