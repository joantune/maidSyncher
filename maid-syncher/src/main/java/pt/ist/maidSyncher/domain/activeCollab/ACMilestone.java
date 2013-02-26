package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACMilestone extends ACMilestone_Base {

    public  ACMilestone() {
        super();
    }

//    static ACMilestone process(pt.ist.maidSyncher.api.activeCollab.ACMilestone milestone) {
//        checkNotNull(milestone);
//        return (ACMilestone) findOrCreateAndProccess(milestone, ACMilestone.class, MaidRoot.getInstance().getAcObjects());
//    }

    @Service
    public static ACMilestone process(pt.ist.maidSyncher.api.activeCollab.ACMilestone milestone, ACProject project) {
        checkNotNull(milestone);
        ACMilestone acMilestone =
                (ACMilestone) findOrCreateAndProccess(milestone, ACMilestone.class, MaidRoot.getInstance().getAcObjects());
        pt.ist.maidSyncher.domain.activeCollab.ACProject acProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);
        acMilestone.setProject(acProject);
        return acMilestone;
    }

    public static ACMilestone findById(long id) {
        return (ACMilestone) MiscUtils.findACObjectsById(id, ACMilestone.class);
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }


}
