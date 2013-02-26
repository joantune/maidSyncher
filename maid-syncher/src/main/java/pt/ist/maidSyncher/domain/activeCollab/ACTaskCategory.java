package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACCategory;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACTaskCategory extends ACTaskCategory_Base {

    public  ACTaskCategory() {
        super();
    }

    @Service
    static ACTaskCategory process(ACCategory category) {
        checkNotNull(category);
        return (ACTaskCategory) findOrCreateAndProccess(category, ACTaskCategory.class, MaidRoot.getInstance().getAcObjects());
    }

    @Service
    public static void process(Set<ACCategory> categories, ACProject project) {
        checkNotNull(project);
        //let's get the project
        pt.ist.maidSyncher.domain.activeCollab.ACProject acProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);

        //let's take care of each category
        Set<ACTaskCategory> oldTaskCategoriesDefined = new HashSet<ACTaskCategory>(acProject.getTaskCategoriesDefined());
        Set<ACTaskCategory> newTaskCategoriesDefined = new HashSet<ACTaskCategory>();

        for (ACCategory category : categories) {
            ACTaskCategory acTaskCategory = process(category);
            newTaskCategoriesDefined.add(acTaskCategory);
        }

        //let's remove all of them
        for (ACTaskCategory acTaskCategory : acProject.getTaskCategoriesDefined()) {
            acProject.removeTaskCategoriesDefined(acTaskCategory);
        }
        for (ACTaskCategory acTaskCategory : newTaskCategoriesDefined)
            acProject.addTaskCategoriesDefined(acTaskCategory);

    }

    public static ACTaskCategory findById(long id) {
        return (ACTaskCategory) MiscUtils.findACObjectsById(id, ACTaskCategory.class);
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }

}
