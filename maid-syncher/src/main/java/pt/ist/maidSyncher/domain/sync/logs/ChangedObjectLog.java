package pt.ist.maidSyncher.domain.sync.logs;

import pt.ist.maidSyncher.domain.SynchableObject;

public class ChangedObjectLog extends ChangedObjectLog_Base {

    public ChangedObjectLog() {
        super();
    }

    public ChangedObjectLog(SynchableObject synchableObject) {
        setClassName(synchableObject.getClass().getName());
        setUrlObject(synchableObject.getHtmlUrl());
    }

}
