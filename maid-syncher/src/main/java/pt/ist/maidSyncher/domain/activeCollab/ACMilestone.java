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
package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.MilestoneService;

import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.utils.MiscUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class ACMilestone extends ACMilestone_Base {

    private static final String DSC_PRIORITY = "priority";
    private static final String DSC_ASSIGNEEID = "assigneeId";
    private static final String DSC_PROJECTID = "projectId";
    private static final String DSC_NAME = "name";
    private static final String DSC_BODY = "body";
    private static final String DSC_DUEON = "dueOn";

    public  ACMilestone() {
        super();
    }

//    static ACMilestone process(pt.ist.maidSyncher.api.activeCollab.ACMilestone milestone) {
//        checkNotNull(milestone);
//        return (ACMilestone) findOrCreateAndProccess(milestone, ACMilestone.class, MaidRoot.getInstance().getAcObjects());
//    }

    public static ACMilestone process(pt.ist.maidSyncher.api.activeCollab.ACMilestone milestone, ACProject project) {
        checkNotNull(milestone);
        ACMilestone acMilestone =
                (ACMilestone) findOrCreateAndProccess(milestone, ACMilestone.class, MaidRoot.getInstance().getAcObjectsSet());
        pt.ist.maidSyncher.domain.activeCollab.ACProject acProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);
        acMilestone.setProject(acProject);
        return acMilestone;
    }

    public static ACMilestone process(pt.ist.maidSyncher.api.activeCollab.ACMilestone milestone, boolean skipSync) {
        checkNotNull(milestone);
        ACMilestone acMilestone =
                (ACMilestone) findOrCreateAndProccess(milestone, ACMilestone.class, MaidRoot.getInstance().getAcObjectsSet(),
                        skipSync);

        pt.ist.maidSyncher.domain.activeCollab.ACProject acProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.findById(milestone.getProjectId());
        checkNotNull(acProject);
        acMilestone.setProject(acProject);
        return acMilestone;
    }

    public static ACMilestone findMilestone(final pt.ist.maidSyncher.domain.activeCollab.ACProject acProject,
            final String milestoneName) {
        return (ACMilestone) Iterables.tryFind(MaidRoot.getInstance().getAcObjectsSet(), new Predicate<ACObject>() {
            @Override
            public boolean apply(ACObject input) {
                if (input == null)
                    return false;
                if (input instanceof ACMilestone) {
                    ACMilestone acMilestone = (ACMilestone) input;
                    return ObjectUtils.equals(acMilestone.getProject(), acProject)
                            && (StringUtils.equalsIgnoreCase(acMilestone.getName(), milestoneName));
                }
                return false;
            }
        }).orNull();

    }

    public static ACMilestone findById(long id) {
        return (ACMilestone) MiscUtils.findACObjectsById(id, ACMilestone.class);
    }


    @Override
    public DSIObject getDSIObject() {
        return getDsiObjectMilestone();
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

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        checkNotNull(syncEvent);
        checkArgument(syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.GITHUB));
        SyncActionWrapper<GHMilestone> syncActionWrapperToReturn = null;
        if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
            syncActionWrapperToReturn = syncCreateEvent(syncEvent);
        } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
            syncActionWrapperToReturn = syncUpdateEvent(syncEvent);

        }

        return syncActionWrapperToReturn;

    }

    private SyncActionWrapper<GHMilestone> syncUpdateEvent(final SyncEvent syncEvent) {
        final Set<String> tickedDescriptors = new HashSet<>();
        boolean auxChangedName = false;
        boolean auxChangedDueOn = false;
        boolean auxChangedBody = false;

        for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor) {
            case DSC_PERMALINK:
            case DSC_ID:
            case DSC_URL:
            case DSC_PRIORITY:
            case DSC_ASSIGNEEID:
            case DSC_PROJECTID:
                //non code related things to complete
                //only non relevant descriptors above
                break;
            case DSC_BODY:
                //we should update the bpdy
                auxChangedBody = true;
                break;
            case DSC_DUEON:
                auxChangedDueOn = true;
                //the due date as well
                break;
            case DSC_NAME:
                //and the name
                auxChangedName = true;
                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        final boolean changedName = auxChangedName;
        final boolean changedBody = auxChangedBody;
        final boolean changedDueOn = auxChangedDueOn;

        return new SyncActionWrapper<GHMilestone>() {

            @Override
            public Collection<GHMilestone> sync() throws IOException {
                Multimap<GHRepository, Milestone> milestonesToEdit = null;
                Set<GHMilestone> ghMilestonesChanged = new HashSet<>();

                if (changedName) {
                    milestonesToEdit = getNewPrefilledGHMilestonesToEdit(milestonesToEdit);
                    //let's make sure we edit all of its names
                    for (Milestone milestone : milestonesToEdit.values()) {
                        milestone.setTitle(getName());
                    }

                }
                if (changedBody) {
                    milestonesToEdit = getNewPrefilledGHMilestonesToEdit(milestonesToEdit);
                    //let's make sure we edit all of its bodies
                    for (Milestone milestone : milestonesToEdit.values()) {
                        milestone.setDescription(getBody());
                    }

                }
                if (changedDueOn) {
                    milestonesToEdit = getNewPrefilledGHMilestonesToEdit(milestonesToEdit);
                    //let's make sure we edit all of its due on
                    for (Milestone milestone : milestonesToEdit.values()) {
                        milestone.setDueOn(getDueOn().toDate());
                    }

                }
                if (milestonesToEdit != null) {
                    MilestoneService milestoneService = new MilestoneService(MaidRoot.getGitHubClient());
                    for (GHRepository ghRepository : milestonesToEdit.keySet()) {
                        for (Milestone milestone : milestonesToEdit.get(ghRepository)) {
                            ghMilestonesChanged.add(GHMilestone.process(milestoneService.editMilestone(ghRepository, milestone),
                                    true));

                        }
                    }
                }
                return ghMilestonesChanged;
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
                Set<Class> classesDependedOn = new HashSet<>();
                classesDependedOn.add(GHRepository.class);
                return classesDependedOn;
            }

            @Override
            public Collection<String> getPropertyDescriptorNamesTicked() {
                return tickedDescriptors;
            }
        };

    }

    private Multimap<GHRepository, Milestone> getNewPrefilledGHMilestonesToEdit(Multimap<GHRepository, Milestone> milestonesToEdit) {
        if (milestonesToEdit != null)
            return milestonesToEdit; //it's already inited
        Multimap<GHRepository, Milestone> newMilestonesToEdit = HashMultimap.create();
        //let's get the dsiMilestone and from there all of the GHMilestones, prefill them, add them to
        //the newMilestonesToEdit, and return them
        DSIMilestone dsiMilestone = (DSIMilestone) getDSIObject();
        checkNotNull(dsiMilestone);
        Set<GHMilestone> ghMilestonesSet = dsiMilestone.getGhMilestonesSet();
        checkNotNull(ghMilestonesSet);

        for (GHMilestone ghMilestoneToEdit : ghMilestonesSet) {
            Milestone milestone = new Milestone();
            try {
                ghMilestoneToEdit.copyPropertiesTo(milestone);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                throw new Error(e);
            }
            newMilestonesToEdit.put(ghMilestoneToEdit.getRepository(), milestone);
        }
        return newMilestonesToEdit;

    }

    private SyncActionWrapper<GHMilestone> syncCreateEvent(final SyncEvent syncEvent) {
        return new EmptySyncActionWrapper(syncEvent);
    }

}
