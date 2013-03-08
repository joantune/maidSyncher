package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIUser;
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
    protected DSIObject getDSIObject() {
        return getDsiObjectFromACUser();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null) {
            dsiObject = new DSIUser();
        }

        return dsiObject;
    }

}
