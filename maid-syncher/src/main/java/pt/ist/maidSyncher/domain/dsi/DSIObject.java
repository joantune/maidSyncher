package pt.ist.maidSyncher.domain.dsi;

import org.joda.time.LocalTime;

import pt.ist.maidSyncher.domain.MaidRoot;

public class DSIObject extends DSIObject_Base {

    public  DSIObject() {
        super();
        setLastUpdatedAt(new LocalTime());
        setMaidRoot(MaidRoot.getInstance());
    }


}
