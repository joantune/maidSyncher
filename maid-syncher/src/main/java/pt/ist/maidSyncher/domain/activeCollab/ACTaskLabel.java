package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACTaskLabel extends ACTaskLabel_Base {

    public ACTaskLabel() {
        super();
        setAcInstance(MaidRoot.getInstance().getAcInstance());
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync) {
        // TODO Auto-generated method stub

    }


    @Service
    public static ACTaskLabel process(pt.ist.maidSyncher.api.activeCollab.ACTaskLabel acTaskLabel) {
        checkNotNull(acTaskLabel);

        MaidRoot instance = MaidRoot.getInstance();
        return (ACTaskLabel) findOrCreateAndProccess(acTaskLabel, ACTaskLabel.class, instance.getAcObjects());
    }

    public static ACTaskLabel findById(long id) {
        return (ACTaskLabel) MiscUtils.findACObjectsById(id, ACTaskLabel.class);
    }

}
