package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;

public class ACProjectLabel extends ACProjectLabel_Base {

    public  ACProjectLabel() {
        super();
        setAcInstance(MaidRoot.getInstance().getAcInstance());
    }

    @Service
    public static ACProjectLabel process(pt.ist.maidSyncher.api.activeCollab.ACProjectLabel acProjectLabel)
    {
        checkNotNull(acProjectLabel);
        MaidRoot maidRoot = MaidRoot.getInstance();

        return (ACProjectLabel) findOrCreateAndProccess(acProjectLabel, ACProjectLabel.class, maidRoot.getAcObjects());
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }


}
