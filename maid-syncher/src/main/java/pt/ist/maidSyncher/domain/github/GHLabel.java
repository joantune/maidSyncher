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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.joda.time.LocalTime;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class GHLabel extends GHLabel_Base {

    public final static String PROJECT_PREFIX = "P-";

    public final static String DELETED_LABEL_NAME = "deleted";

    public  GHLabel() {
        super();
        MaidRoot.getInstance().addGhLabels(this);
    }

    static GHLabel process(Label label) {
        return process(label, false);

    }

    public static Set<GHLabel> getAllLabelsWith(final String name) {
        MaidRoot maidRoot = MaidRoot.getInstance();
        return Sets.filter(maidRoot.getGhLabelsSet(), new Predicate<GHLabel>() {
            @Override
            public boolean apply(GHLabel ghLabel) {
                if (ghLabel == null)
                    return false;
                if (ghLabel.getName().equals(name)) {
                    return true;
                }
                return false;
            }
        });

    }

    public static boolean containsDeletedLabel(Collection<GHLabel> labelsToSearch) {
        return Iterables.any(labelsToSearch, new Predicate<GHLabel>() {
            @Override
            public boolean apply(GHLabel ghLabel) {
                if (ghLabel == null)
                    return false;
                return ghLabel.getName().equalsIgnoreCase(DELETED_LABEL_NAME);

            }
        });
    }

    public static void process(Collection<Label> labels, Repository repository) {
        checkNotNull(labels);

        //each label belongs to a given repository

        //now the rep
        GHRepository ghRepository = GHRepository.process(repository);

        //let's get the old ones
        Set<GHLabel> oldGhLabels = new HashSet<GHLabel>(ghRepository.getLabelsDefined());

        Set<GHLabel> newGhLabels = new HashSet<GHLabel>();

        for (Label labelToProcess : labels) {
            newGhLabels.add(process(labelToProcess));
        }

        //let us remove the old relations
        ghRepository.getLabelsDefined().clear();
        //add the new ones
        ghRepository.getLabelsDefined().addAll(newGhLabels);
        //And make sync DELETEd events for the removed ones
        oldGhLabels.removeAll(newGhLabels);
        for (GHLabel removedGHLabel : oldGhLabels) {
            SyncEvent.createAndAddADeleteEventWithoutAPIObj(removedGHLabel);
        }
    }

    public static GHLabel process(Label label, long repositoryId, boolean skipSync) {
        checkNotNull(label);

        //each label belongs to a given repository

        //let's get a label first
        GHLabel ghLabel = process(label, skipSync);

        //now the rep
        GHRepository ghRepository = GHRepository.findById(repositoryId);
        ghLabel.setRepository(ghRepository);
        return ghLabel;
    }

    protected static GHLabel process(Label label, boolean skipSync) {
        checkNotNull(label);
        MaidRoot maidRoot = MaidRoot.getInstance();
        GHLabel ghLabel =
                (GHLabel) findOrCreateAndProccess(label, GHLabel.class, maidRoot.getGhLabels(), ObjectFindStrategy.FIND_BY_URL);
        return ghLabel;
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

        return getDsiObjectProject();
    }


    @Override
    public DSIObject findOrCreateDSIObject() {
        //let us always return null, the relation
        //with a DSIObject must be filled by a sync
        //action, because we must see if this label
        //relates to an ACProject [depended on the name]
        //and that must be done when both parts are synched, not
        //meanwhile Issue #12 GH
        return null;
    }

    public void delete() {
        //let us remove all of the issues, and rep
        removeRepository();
        removeDsiObjectProject();
        removeMaidRootFromLabel();
        for (GHIssue issue : getIssues()) {
            removeIssues(issue);
        }
        deleteDomainObject();

    }

}
