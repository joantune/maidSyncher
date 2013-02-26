package pt.ist.maidSyncher.domain.activeCollab;

import pt.ist.maidSyncher.domain.MaidRoot;


public abstract class ACObject extends ACObject_Base {

    public  ACObject() {
        super();
        if (MaidRoot.getInstance() != null)
            MaidRoot.getInstance().addAcObjects(this);
    }


}
