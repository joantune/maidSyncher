package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACUser extends ACUser_Base {

    public  ACUser() {
        super();
    }

    public static ACUser process(pt.ist.maidSyncher.api.activeCollab.ACUser acUser) {
        checkNotNull(acUser);
        return (ACUser) findOrCreateAndProccess(acUser, ACUser.class, MaidRoot.getInstance().getAcObjects());
    }

    public static ACUser findById(long id) {
        return (ACUser) MiscUtils.findACObjectsById(id, ACUser.class);
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }

}
