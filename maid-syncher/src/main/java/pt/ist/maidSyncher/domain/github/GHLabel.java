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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class GHLabel extends GHLabel_Base {

    public final static Logger LOGGER = LoggerFactory.getLogger(GHLabel.class);

  public final static String PROJECT_PREFIX = "P-";

    public final static String DELETED_LABEL_NAME = "deleted";

    public GHLabel() {
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

    @Override
    public String getHtmlUrl() {
        //we should have no HTML Url, let's just make sure :)
        if (super.getHtmlUrl() != null) {
            LOGGER.warn("HtmlUrl slot is not empty as it should..");
        }
        if (getRepository() != null && getRepository().getHtmlUrl() != null)
            return getRepository().getHtmlUrl() + LABEL_HTML_URL_SUFFIX;
        return null;
    }

    private final static String LABEL_HTML_URL_SUFFIX = "/issues";

    public static void process(Collection<Label> labels, Repository repository) {
        checkNotNull(labels);

        //each label belongs to a given repository

        //now the rep
        GHRepository ghRepository = GHRepository.process(repository);

        //let's get the old ones
        Set<GHLabel> oldGhLabels = new HashSet<GHLabel>(ghRepository.getLabelsDefinedSet());

        Set<GHLabel> newGhLabels = new HashSet<GHLabel>();

        for (Label labelToProcess : labels) {
            GHLabel processedLabel = process(labelToProcess);
            newGhLabels.add(processedLabel);
        }

        //let us remove the old relations
        ghRepository.getLabelsDefinedSet().clear();
        //add the new ones
        ghRepository.getLabelsDefinedSet().addAll(newGhLabels);
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
                (GHLabel) findOrCreateAndProccess(label, GHLabel.class, maidRoot.getGhLabelsSet(), ObjectFindStrategy.FIND_BY_URL);
        return ghLabel;
    }

    @Override
    public DateTime getUpdatedAtDate() {
        /*we have no updated at filed (which is no big deal, so, let's make
         * this have less priority [either return the creation date or
         * the date of the last time it was synched] */
        return getLastSynchTime() == null ? getCreatedAt() : getLastSynchTime();
    }

    @Override
    public DSIObject getDSIObject() {

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

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        //TODO delete
        return new EmptySyncActionWrapper(syncEvent);
    }

    public void delete() {
        //let us remove all of the issues, and rep
        setRepository(null);
        setDsiObjectProject(null);
        setMaidRootFromLabel(null);
        for (GHIssue issue : getIssuesSet()) {
            removeIssues(issue);
        }
        deleteDomainObject();

    }

}
