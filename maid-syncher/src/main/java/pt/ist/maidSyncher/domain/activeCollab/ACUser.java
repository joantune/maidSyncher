package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACUser extends ACUser_Base {

    public  ACUser() {
        super();
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync) {
        // TODO Auto-generated method stub

    }

    public static ACUser process(pt.ist.maidSyncher.api.activeCollab.ACUser acUser) {
        checkNotNull(acUser);
        return (ACUser) findOrCreateAndProccess(acUser, ACUser.class, MaidRoot.getInstance().getAcObjects());
    }

    public static ACUser findById(long id) {
        return (ACUser) MiscUtils.findACObjectsById(id, ACUser.class);
    }

}
