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

import java.util.Set;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.joda.time.LocalTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

public class GHLabel extends GHLabel_Base {

    public final static String PROJECT_PREFIX = "P-";

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

    @Service
    public static GHLabel process(Label label, Repository repository) {
        checkNotNull(label);

        //each label belongs to a given repository

        //let's get a label first
        GHLabel ghLabel = process(label);

        //now the rep
        GHRepository ghRepository = GHRepository.process(repository);
        ghLabel.setRepository(ghRepository);
        return ghLabel;
    }

    @Service
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
        //action
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
