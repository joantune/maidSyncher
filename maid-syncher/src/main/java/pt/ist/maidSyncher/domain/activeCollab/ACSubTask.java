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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSISubTask;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;

public class ACSubTask extends ACSubTask_Base {

    public ACSubTask() {
        super();
    }

    private static ACSubTask process(pt.ist.maidSyncher.api.activeCollab.ACSubTask acSubTask) {
        checkNotNull(acSubTask);
        return (ACSubTask) findOrCreateAndProccess(acSubTask, ACSubTask.class, MaidRoot.getInstance().getAcObjects());
    }

    @Override
    public Collection<PropertyDescriptor> copyPropertiesFrom(Object orig) throws IllegalAccessException,
    InvocationTargetException, NoSuchMethodException, TaskNotVisibleException {
        Collection<PropertyDescriptor> propertyDescriptorsToReturn = super.copyPropertiesFrom(orig);
        pt.ist.maidSyncher.api.activeCollab.ACSubTask acSubTask = (pt.ist.maidSyncher.api.activeCollab.ACSubTask) orig;
        if (acSubTask.getParentClass().equals(ACTask.CLASS_VALUE)) {
            pt.ist.maidSyncher.domain.activeCollab.ACTask acTask = pt.ist.maidSyncher.domain.activeCollab.ACTask.findById(acSubTask.getParentId());
            pt.ist.maidSyncher.domain.activeCollab.ACTask oldTask = getTask();
            setTask(acTask);
            if (!ObjectUtils.equals(acTask, oldTask)) {
                //let's add the PropertyDescriptor
                propertyDescriptorsToReturn.add(getPropertyDescriptorAndCheckItExists(acSubTask, "parentId"));

            }
        }
        return propertyDescriptorsToReturn;
    }

    public static void process(Set<pt.ist.maidSyncher.api.activeCollab.ACSubTask> acSubTasks, ACTask acTask) {
        pt.ist.maidSyncher.domain.activeCollab.ACTask acDomainTask;
        try {
            acDomainTask = pt.ist.maidSyncher.domain.activeCollab.ACTask.process(acTask);
        } catch (TaskNotVisibleException e) {
            return;
        }

        //let's get all of the sub tasks
        Set<ACSubTask> newSubTaskSet = new HashSet<>();

        Set<ACSubTask> oldSubTaskSet = new HashSet<>(acDomainTask.getSubTasks());
        for (pt.ist.maidSyncher.api.activeCollab.ACSubTask subTask : acSubTasks) {
            ACSubTask domainSubTask = process(subTask);
            newSubTaskSet.add(domainSubTask);
        }

        //ok, now let's remove all of the old ones and add the new ones TODO sync if changes were made
        for (ACSubTask oldSubTask : acDomainTask.getSubTasks()) {
            acDomainTask.removeSubTasks(oldSubTask);
        }

        for (ACSubTask newSubTask : newSubTaskSet) {
            acDomainTask.addSubTasks(newSubTask);
        }

    }

    @Override
    protected DSIObject getDSIObject() {
        return getDsiObjectSubTask();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null) {
            dsiObject = new DSISubTask((DSIIssue) getTask().getDSIObject());

        }
        return dsiObject;
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        // TODO Auto-generated method stub
        return null;
    }

}
