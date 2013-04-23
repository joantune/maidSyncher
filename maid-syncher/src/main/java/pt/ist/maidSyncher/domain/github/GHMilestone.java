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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.joda.time.LocalTime;

import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class GHMilestone extends GHMilestone_Base {

    public GHMilestone() {
        super();
        MaidRoot.getInstance().addGhMilestones(this);
    }

    static GHMilestone process(Milestone milestone) {
        checkNotNull(milestone);
        MaidRoot maidRoot = MaidRoot.getInstance();

        GHMilestone ghMilestone =
                (GHMilestone) findOrCreateAndProccess(milestone, GHMilestone.class, maidRoot.getGhMilestones(),
                        ObjectFindStrategy.FIND_BY_URL);
        return ghMilestone;
    }

    public static void process(Collection<Milestone> milestones, Repository repository) {
        MaidRoot maidRoot = MaidRoot.getInstance();

        //let's take care of the repository
        GHRepository ghRepository = GHRepository.process(repository);

        //retrieving the list of current milestones
        Set<GHMilestone> oldGHMilestones = new HashSet<GHMilestone>(ghRepository.getMilestones());

        Set<GHMilestone> newGHMilestones = new HashSet<GHMilestone>();

        for (Milestone newAPIMilestone : milestones) {
            GHMilestone ghMilestone = process(newAPIMilestone);
            newGHMilestones.add(ghMilestone);
        }

        //remove the old ones and set the new ones
        ghRepository.getMilestones().clear();
        ghRepository.getMilestones().addAll(newGHMilestones);

        //create the DELETE events
        oldGHMilestones.removeAll(newGHMilestones);

        for (GHMilestone removedGHMilestone : oldGHMilestones) {
            SyncEvent.createAndAddADeleteEventWithoutAPIObj(removedGHMilestone);

        }

    }

    @Override
    public LocalTime getUpdatedAtDate() {
        /*we have no updated at filed (which is no big deal, so, let's make
         * this have less priority [either return the creation date or
         * the date of the last time it was synched] */
        return getLastSynchTime() == null ? getCreatedAt() : getLastSynchTime();
    }


    @Override
    protected DSIObject getDSIObject() {
        return getDsiObjectMilestone();
    }

    protected ACMilestone getACCorrespondingPreliminarObject(long projectId) {
        checkArgument(projectId > 0);
        ACMilestone acMilestone = new ACMilestone();
        acMilestone.setName(getTitle());
        acMilestone.setBody(getDescription());
        acMilestone.setDueOn(getDueOn().toDateTimeToday().toDate());
        acMilestone.setProjectId(projectId);
        return acMilestone;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null)
            dsiObject = new DSIMilestone();
        return dsiObject;
    }


}
