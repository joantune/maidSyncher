package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

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
    protected DSIObject getDSIObject() {
        return null;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        throw new UnsupportedOperationException();
    }

}
