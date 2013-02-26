package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;

import jvstm.cps.ConsistencyPredicate;

import org.apache.commons.lang.ObjectUtils;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;

public class ACLoggedTime extends ACLoggedTime_Base {

    public  ACLoggedTime() {
        super();
    }

    @Override
    public Collection<PropertyDescriptor> copyPropertiesFrom(Object orig) throws IllegalAccessException,
    InvocationTargetException,
    NoSuchMethodException, TaskNotVisibleException {
        HashSet<PropertyDescriptor> changedPropertyDescriptor = new HashSet<PropertyDescriptor>(super.copyPropertiesFrom(orig));
        pt.ist.maidSyncher.api.activeCollab.ACLoggedTime acLoggedTime = (pt.ist.maidSyncher.api.activeCollab.ACLoggedTime) orig;

        //let's get the relevant parent
        switch (acLoggedTime.getParentClass()) {
        case ACTask.CLASS_VALUE:
            pt.ist.maidSyncher.domain.activeCollab.ACTask task = null;
            try {
                task = pt.ist.maidSyncher.domain.activeCollab.ACTask.findById(acLoggedTime.getParentId());
            } catch (java.lang.IllegalStateException ex) {
                throw new TaskNotVisibleException(ex);
            }
            pt.ist.maidSyncher.domain.activeCollab.ACTask oldTask = getTask();
            setTask(task);
            if (!ObjectUtils.equals(oldTask, task)) {
                changedPropertyDescriptor.add(getPropertyDescriptorAndCheckItExists(orig, "parentId"));
            }
            break;
        case ACProject.CLASS_VALUE:
            pt.ist.maidSyncher.domain.activeCollab.ACProject project =
            pt.ist.maidSyncher.domain.activeCollab.ACProject.findById(acLoggedTime.getParentId());
            pt.ist.maidSyncher.domain.activeCollab.ACProject oldProject = getProject();
            setProject(project);
            if (!ObjectUtils.equals(oldProject, project)) {
                changedPropertyDescriptor.add(getPropertyDescriptorAndCheckItExists(orig, "parentId"));
            }

            break;
        }
        return changedPropertyDescriptor;
    }

    @ConsistencyPredicate
    private boolean checkHasParent() {
        return hasProject() || hasTask();
    }

    @Service
    public static ACLoggedTime process(pt.ist.maidSyncher.api.activeCollab.ACLoggedTime acLoggedTime) {
        checkNotNull(acLoggedTime);
        if (isProccessable(acLoggedTime) == false)
            return null;
        ACLoggedTime acDomainLoggedTime = null;
        acDomainLoggedTime =
                (ACLoggedTime) findOrCreateAndProccess(acLoggedTime, ACLoggedTime.class, MaidRoot.getInstance().getAcObjects());
        return acDomainLoggedTime;
    }

    private static boolean isProccessable(pt.ist.maidSyncher.api.activeCollab.ACLoggedTime acLoggedTime) {
        switch (acLoggedTime.getParentClass()) {
        case ACTask.CLASS_VALUE:
            pt.ist.maidSyncher.domain.activeCollab.ACTask task = null;
            try {
                task = pt.ist.maidSyncher.domain.activeCollab.ACTask.findById(acLoggedTime.getParentId());
            } catch (java.lang.IllegalStateException ex) {
                return false;
            }
            break;
        case ACProject.CLASS_VALUE:
            break;
        }
        return true;
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }

}
