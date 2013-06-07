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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIComment;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class ACComment extends ACComment_Base {

    public  ACComment() {
        super();
    }

    private static ACComment process(pt.ist.maidSyncher.api.activeCollab.ACComment acComment) {
        checkNotNull(acComment);
        return (ACComment) findOrCreateAndProccess(acComment, ACComment.class, MaidRoot.getInstance().getAcObjectsSet());
    }

    @Override
    public Collection<String> copyPropertiesFrom(Object orig) throws IllegalAccessException,
    InvocationTargetException, NoSuchMethodException, TaskNotVisibleException {
        HashSet<String> changedDescriptorsToReturn = new HashSet<>();
        changedDescriptorsToReturn.addAll(super.copyPropertiesFrom(orig));
        pt.ist.maidSyncher.api.activeCollab.ACComment acComment = (pt.ist.maidSyncher.api.activeCollab.ACComment) orig;
        if (acComment.getParentClass().equals(ACTask.CLASS_VALUE)) {
            pt.ist.maidSyncher.domain.activeCollab.ACTask oldTask = getTask();
            pt.ist.maidSyncher.domain.activeCollab.ACTask newTask =
                    pt.ist.maidSyncher.domain.activeCollab.ACTask.findById(acComment.getParentId());
            setTask(newTask);
            if (!ObjectUtils.equals(newTask, oldTask)) {
                changedDescriptorsToReturn.add(getPropertyDescriptorNameAndCheckItExists(acComment, "parentId"));
            }
        }
        return changedDescriptorsToReturn;
    }

    public static void process(Set<pt.ist.maidSyncher.api.activeCollab.ACComment> acComments, ACTask acTask) {
        pt.ist.maidSyncher.domain.activeCollab.ACTask task;
        try {
            task = pt.ist.maidSyncher.domain.activeCollab.ACTask.process(acTask);
        } catch (TaskNotVisibleException e) {
            return;
        }

        //let's proccess all of the comments now

        Set<ACComment> acOldComments = new HashSet<ACComment>(task.getCommentsSet());

        Set<ACComment> acNewComments = new HashSet<ACComment>();
        for (pt.ist.maidSyncher.api.activeCollab.ACComment acComment : acComments) {
            acNewComments.add(process(acComment));
        }

        //TODO check, and sync, if there were deleted comments

        for (ACComment acComment : task.getCommentsSet()) {
            task.removeComments(acComment);
        }

        for (ACComment acComment : acNewComments) {
            task.addComments(acComment);
        }

    }

    @Override
    public DSIObject getDSIObject() {
        return getDsiObjectComment();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null) {
            dsiObject = new DSIComment((DSIIssue) getTask().getDSIObject());
            setDsiObjectComment((DSIComment) dsiObject);
        }

        return dsiObject;
    }

}
