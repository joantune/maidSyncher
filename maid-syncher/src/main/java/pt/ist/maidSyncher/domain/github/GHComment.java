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

import jvstm.cps.ConsistencyPredicate;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.joda.time.LocalTime;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class GHComment extends GHComment_Base {

    public GHComment() {
        super();
        MaidRoot.getInstance().addGhComments(this);
    }


    public static void process(Collection<Comment> comments, Issue issue) {
        checkNotNull(comments);
        checkNotNull(issue);

        MaidRoot maidRoot = MaidRoot.getInstance();

        //let us get the issue
        GHIssue ghIssue = GHIssue.process(issue);

        //the old comments
        Set<GHComment> oldComments = new HashSet<GHComment>(ghIssue.getCommentsSet());

        Set<GHComment> newComments = new HashSet<>();

        for (Comment commentToProcess : comments) {
            GHComment ghComment =
                    (GHComment) findOrCreateAndProccess(commentToProcess, GHComment.class, maidRoot.getGhCommentsSet());
            newComments.add(ghComment);
        }

        //remove the old ones
        ghIssue.getCommentsSet().clear();
        ghIssue.getCommentsSet().addAll(newComments);

        //create DELETE SyncEvents for the removed ones
        oldComments.removeAll(newComments);

        for (GHComment commentToRemove : oldComments) {
            SyncEvent.createAndAddADeleteEventWithoutAPIObj(commentToRemove);
        }
    }


    @ConsistencyPredicate
    private boolean checkMultiplicityDsiObject() {

        //we must have only one of them (because this is either a comment, logged time, or sub task) or zero
        boolean hasDsiObject = false;
        if (getDsiObjectComment() != null)
            hasDsiObject = true;
        if (getDsiObjectLoggedTime() != null) {
            if (hasDsiObject)
                return false;
            hasDsiObject = true;
        }
        return true; //we also allow having no dsi object (which should be temporary)
    }

    @Override
    protected DSIObject getDSIObject() {
        if (getDsiObjectComment() != null)
            return getDsiObjectComment();
        if (getDsiObjectLoggedTime() != null)
            return getDsiObjectLoggedTime();
        return null;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null)
            //TODO
            return null;
        return null;
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        return getUpdatedAtDate() == null ? getCreatedAt() : getUpdatedAt();
    }


}
