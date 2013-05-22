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

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.domain.activeCollab.ACObject;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.github.GHObject;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 4 de Mar de 2013
 * 
 * 
 */
public class SyncEvent {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncEvent.class);

    public static enum TypeOfChangeEvent {
        CREATE, READ, UPDATE, DELETE;
    }

    public static enum SyncUniverse {
        ACTIVE_COLLAB, GITHUB;

        /**
         * 
         * @param synchOrigin
         * @return the {@link SyncUniverse}, based on the idea that if the Origin is of one type, the target is the opposite
         */
        static public SyncUniverse getTargetSyncUniverse(SynchableObject synchOrigin) {
            checkNotNull(synchOrigin);
            if (synchOrigin instanceof ACObject)
                return GITHUB;
            if (synchOrigin instanceof GHObject)
                return ACTIVE_COLLAB;
            return null;
        }
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
            DSIObject dsiObject, APIObjectWrapper apiObjectWrapper, SyncUniverse targetSyncUniverse, SynchableObject origin) {
        this.dateOfChange = dateOfChange;
        this.typeOfChangeEvent = changeEvent;
        this.changedPropertyDescriptors = new HashSet<PropertyDescriptor>(propertyDescriptors);
        this.dsiElement = dsiObject;
        this.apiObjectWrapper = apiObjectWrapper;
        this.targetSyncUniverse = targetSyncUniverse;
        this.originObject = origin;

    }

    public static SyncEvent createAndAddADeleteEventWithoutAPIObj(SynchableObject removedObject) {
        SyncUniverse syncUniverseToUse = SyncUniverse.getTargetSyncUniverse(removedObject);
        SyncEvent syncEvent =
                new SyncEvent(removedObject.getUpdatedAtDate(), TypeOfChangeEvent.DELETE,
                        Collections.<PropertyDescriptor> emptySet(), removedObject.getDSIObject(), new APIObjectWrapper() {

                    @Override
                    public void validateAPIObject() throws SyncEventOriginObjectChanged {
                        //we have no APIObject :) it was deleted, there is none
                        return;
                    }

                    @Override
                    public Object getAPIObject() {
                        return null;
                    }
                }, syncUniverseToUse, removedObject);
        SynchableObject.addSyncEvent(syncEvent);
        return syncEvent;

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
        String stringToReturn =
                "Sync event, DSIElement: " + getDsiElement().getExternalId() + " (" + getDsiElement().getClass().getSimpleName()
                + ")" + " Type: " + getTypeOfChangeEvent() + " targetUniverse: " + getTargetSyncUniverse()
                + " originObject: " + getOriginObject().getClass().getSimpleName();

        if (getChangedPropertyDescriptors() != null && getChangedPropertyDescriptors().isEmpty() == false) {
            //let's add the property descriptors changed
            stringToReturn += " Changed descriptors: ";
            for (PropertyDescriptor propertyDescriptor : getChangedPropertyDescriptors()) {
                stringToReturn += " " + propertyDescriptor.getName();
            }
        }

        return stringToReturn;
    }

    public APIObjectWrapper getApiObjectWrapper() {
        return apiObjectWrapper;
    }

    public static boolean isAbleToRunNow(SyncActionWrapper<? extends SynchableObject> syncActionWrapper,
            Set<DSIObject> dsiObjectsToSync) {
        checkNotNull(dsiObjectsToSync);
        checkNotNull(syncActionWrapper);

        //let's check the classes of depended objects first, as the getSyncDependedOn might fail

        final Set<Class> syncDependedTypesOfDSIObjects = syncActionWrapper.getSyncDependedTypesOfDSIObjects();

        if (Iterables.any(dsiObjectsToSync, new Predicate<DSIObject>() {
            @Override
            public boolean apply(DSIObject input) {
                if (input == null)
                    return false;
                if (syncDependedTypesOfDSIObjects.contains(input.getClass()))
                    return true;
                return false;

            }
        }))
            return false;

        try {
            Collection<DSIObject> syncDependedDSIObjects = syncActionWrapper.getSyncDependedDSIObjects();
            if (Collections.disjoint(syncDependedDSIObjects, dsiObjectsToSync) == false)
                return false;
        } catch (NullPointerException ex) {
            String loggerWarnString = "Got an NPE retrieving syncDependedDSIObjects";
            if (syncActionWrapper == null) {
                loggerWarnString += ". SyncActionWrapper was null!!";
            } else if (syncActionWrapper.getOriginatingSyncEvent() == null) {
                loggerWarnString +=
                        ". Originating SyncEvent of SyncActionWrapper is null! SyncActionWrapper class: "
                                + syncActionWrapper.getClass().getName();
            } else {
                loggerWarnString +=
                        ". SyncEvent of the SyncActionWrapper: " + syncActionWrapper.getOriginatingSyncEvent().toString();
            }
            LOGGER.warn(loggerWarnString, ex);
            return false;
        }

        return true;

    }

}
