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
import jvstm.cps.ConsistencyPredicate;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.joda.time.LocalTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class GHComment extends GHComment_Base {

    public GHComment() {
        super();
        MaidRoot.getInstance().addGhComments(this);
    }

    //TODO receive a list of comments, so that we can detect deletions of comments

    @Service
    public static GHComment process(Comment comment, Issue issue) {
        checkNotNull(comment);
        checkNotNull(issue);

        MaidRoot maidRoot = MaidRoot.getInstance();

        GHComment ghComment = (GHComment) findOrCreateAndProccess(comment, GHComment.class, maidRoot.getGhComments());

        //now let's attach it to the issue
        GHIssue ghIssue = GHIssue.process(issue);

        ghIssue.addComments(ghComment);

        return ghComment;
    }


    @ConsistencyPredicate
    private boolean checkMultiplicityDsiObject() {

        //we must have only one of them (because this is either a comment, logged time, or sub task) or zero
        boolean hasDsiObject = false;
        if (hasDsiObjectComment())
            hasDsiObject = true;
        if (hasDsiObjectLoggedTime()) {
            if (hasDsiObject)
                return false;
            hasDsiObject = true;
        }
        return true; //we also allow having no dsi object (which should be temporary)
    }

    @Override
    protected DSIObject getDSIObject() {
        if (hasDsiObjectComment())
            return getDsiObjectComment();
        if (hasDsiObjectLoggedTime())
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
