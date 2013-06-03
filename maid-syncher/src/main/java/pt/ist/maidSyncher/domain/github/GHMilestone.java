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
package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.joda.time.LocalTime;

import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

public class GHMilestone extends GHMilestone_Base {

    public GHMilestone() {
        super();
        MaidRoot.getInstance().addGhMilestones(this);
    }

    static GHMilestone process(Milestone milestone) {
        return process(milestone, false);
    }

    public static GHMilestone process(Milestone milestone, boolean skipSync) {
        checkNotNull(milestone);
        MaidRoot maidRoot = MaidRoot.getInstance();

        GHMilestone ghMilestone =
                (GHMilestone) findOrCreateAndProccess(milestone, GHMilestone.class, maidRoot.getGhMilestonesSet(), skipSync,
                        ObjectFindStrategy.FIND_BY_URL);
        return ghMilestone;
    }

    public static void process(Collection<Milestone> milestones, Repository repository) {
        MaidRoot maidRoot = MaidRoot.getInstance();

        //let's take care of the repository
        GHRepository ghRepository = GHRepository.process(repository);

        //retrieving the list of current milestones
        Set<GHMilestone> oldGHMilestones = new HashSet<GHMilestone>(ghRepository.getMilestonesSet());

        Set<GHMilestone> newGHMilestones = new HashSet<GHMilestone>();

        for (Milestone newAPIMilestone : milestones) {
            GHMilestone ghMilestone = process(newAPIMilestone);
            newGHMilestones.add(ghMilestone);
        }

        //remove the old ones and set the new ones
        ghRepository.getMilestonesSet().clear();
        ghRepository.getMilestonesSet().addAll(newGHMilestones);

        //create the DELETE events
        oldGHMilestones.removeAll(newGHMilestones);

        for (GHMilestone removedGHMilestone : oldGHMilestones) {
            SyncEvent.createAndAddADeleteEventWithoutAPIObj(removedGHMilestone);

        }

    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        checkNotNull(syncEvent);
        checkArgument(syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.ACTIVE_COLLAB));
        SyncActionWrapper<GHMilestone> syncActionWrapperToReturn = null;
        if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
            syncActionWrapperToReturn = syncCreateEvent(syncEvent);
        } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
            syncActionWrapperToReturn = syncUpdateEvent(syncEvent);

        }

        return syncActionWrapperToReturn;

    }

    static final String DSC_CLOSED_ISSUES = "closedIssues";
    static final String DSC_NUMBER = "number";
    static final String DSC_OPEN_ISSUES = "openIssues";
    static final String DSC_DESCRIPTION = "description";
    static final String DSC_STATE = "state";
    static final String DSC_TITLE = "title";
    static final String DSC_CREATOR = "creator";

    static final String DSC_DUEON = "dueOn";

    private SyncActionWrapper<GHMilestone> syncUpdateEvent(final SyncEvent syncEvent) {
        final Set<PropertyDescriptor> tickedDescriptors = new HashSet<>();
        boolean auxChangedDescription = false;
        boolean auxChangedDueOn = false;
        boolean auxChangedTitle = false;

        for (PropertyDescriptor changedDescriptor : syncEvent.getChangedPropertyDescriptors()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor.getName()) {
            case DSC_URL:
            case DSC_CREATED_AT:
            case DSC_CLOSED_ISSUES:
            case DSC_NUMBER:
            case DSC_OPEN_ISSUES:
            case DSC_CREATOR:
            case DSC_STATE: //we do nothing with the state, because we might have
                //non code related things to complete
                //only non relevant descriptors above
                break;
            case DSC_DESCRIPTION:
                //we should update the description
                auxChangedDescription = true;
                break;
            case DSC_DUEON:
                auxChangedDueOn = true;
                //the due date as well
                break;
            case DSC_TITLE:
                auxChangedTitle = true;
                //and the title
                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        final boolean changedDescription = auxChangedDescription;
        final boolean changedTitle = auxChangedTitle;
        final boolean changedDueOn = auxChangedDueOn;

        return new SyncActionWrapper<GHMilestone>() {

            @Override
            public Collection<GHMilestone> sync() throws IOException {
                Collection<ACMilestone> acMilestonesToEdit = null;
                if (changedDescription) {
                    acMilestonesToEdit = getNewPrefilledACMilestonesToEdit(acMilestonesToEdit);
                    //for each, let's edit the description
                    for (ACMilestone acMilestoneToEdit : acMilestonesToEdit) {
                        acMilestoneToEdit.setBody(getDescription());
                    }

                }
                if (changedTitle) {
                    acMilestonesToEdit = getNewPrefilledACMilestonesToEdit(acMilestonesToEdit);
                    //for each, let's edit the title
                    for (ACMilestone acMilestoneToEdit : acMilestonesToEdit) {
                        acMilestoneToEdit.setName(getTitle());
                    }

                }
                if (changedDueOn) {
                    acMilestonesToEdit = getNewPrefilledACMilestonesToEdit(acMilestonesToEdit);
                    //for each, let's edit the date
                    for (ACMilestone acMilestoneToEdit : acMilestonesToEdit) {
                        acMilestoneToEdit.setDueOn(getDueOn().toDateTimeToday().toDate());
                    }

                }

                if (acMilestonesToEdit != null) {
                    //let's post them all
                    for (ACMilestone acMilestone : acMilestonesToEdit) {
                        ACMilestone newlyUpdatedMilestone = acMilestone.update(acMilestone.getUrl());
                        pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(newlyUpdatedMilestone, true);
                    }
                }

                return Collections.emptyList();
            }

            @Override
            public Collection<PropertyDescriptor> getPropertyDescriptorsTicked() {
                return tickedDescriptors;
            }

            @Override
            public SyncEvent getOriginatingSyncEvent() {
                return syncEvent;
            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                return Collections.emptyList();
            }

            @Override
            public Set<Class> getSyncDependedTypesOfDSIObjects() {
                return Collections.singleton((Class) GHIssue.class);
            }
        };
    }

    private SyncActionWrapper<GHMilestone> syncCreateEvent(SyncEvent syncEvent) {
        return new EmptySyncActionWrapper(syncEvent);
    }

    private Collection<ACMilestone> getNewPrefilledACMilestonesToEdit(Collection<ACMilestone> acMilestones) {

        if (acMilestones != null)
            return acMilestones; //it's already prefilled, let's just return it
        checkNotNull(getDSIObject());
        DSIMilestone dsiMilestone = (DSIMilestone) getDSIObject();
        checkNotNull(dsiMilestone);
        Set<ACMilestone> acMilestonesToEdit = new HashSet<ACMilestone>();

        for (pt.ist.maidSyncher.domain.activeCollab.ACMilestone acMilestoneToCopy : dsiMilestone.getAcMilestonesSet()) {
            ACMilestone acMilestoneToEdit = new ACMilestone();
            try {
                acMilestoneToCopy.copyPropertiesTo(acMilestoneToEdit);
                acMilestonesToEdit.add(acMilestoneToEdit);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                throw new Error("Couldn't prefill an ACMilestone", e);
            }
        }

        return acMilestonesToEdit;
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        /*we have no updated at filed (which is no big deal, so, let's make
         * this have less priority [either return the creation date or
         * the date of the last time it was synched] */
        return getLastSynchTime() == null ? getCreatedAt() : getLastSynchTime();
    }

    @Override
    public DSIObject getDSIObject() {
        return getDsiObjectMilestone();
    }

    protected ACMilestone getACCorrespondingPreliminarObject(long projectId) {
        checkArgument(projectId > 0);
        ACMilestone acMilestone = new ACMilestone();
        acMilestone.setName(getTitle());
        acMilestone.setBody(getDescription());
        if (getDueOn() != null)
            acMilestone.setDueOn(getDueOn().toDateTimeToday().toDate());
        acMilestone.setProjectId(projectId);
        return acMilestone;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null) {
            dsiObject = new DSIMilestone();
            setDsiObjectMilestone((DSIMilestone) dsiObject);
        }
        return dsiObject;
    }

}
