package pt.ist.maidSyncher.domain.activeCollab;

import org.joda.time.LocalTime;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;


public abstract class ACObject extends ACObject_Base {

    public  ACObject() {
        super();
        if (MaidRoot.getInstance() != null)
            MaidRoot.getInstance().addAcObjects(this);
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        if (getUpdatedOn() == null)
            return getCreatedOn();
        else
            return getUpdatedOn();
    }

    @Override
    public void sync(SyncEvent syncEvent) {
        // TODO Auto-generated method stub

    }


}
