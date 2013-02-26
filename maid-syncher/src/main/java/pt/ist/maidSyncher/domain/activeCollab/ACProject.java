package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACProject extends ACProject_Base {

    public  ACProject() {
        super();
        setAcInstance(MaidRoot.getInstance().getAcInstance());
    }

    @Service
    public static ACProject process(pt.ist.maidSyncher.api.activeCollab.ACProject acProject) {
        checkNotNull(acProject);
        ACProject projectToReturn =
                (ACProject) findOrCreateAndProccess(acProject, ACProject.class, MaidRoot.getInstance().getAcObjects());
        return projectToReturn;
    }

    public static ACProject findById(long id) {
        return (ACProject) MiscUtils.findACObjectsById(id, ACProject.class);
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }


}
