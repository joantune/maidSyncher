package pt.ist.maidSyncher.domain.activeCollab;

import pt.ist.maidSyncher.domain.dsi.DSIObject;


public class ACAssignmentLabel extends ACAssignmentLabel_Base {

    public  ACAssignmentLabel() {
        super();
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
