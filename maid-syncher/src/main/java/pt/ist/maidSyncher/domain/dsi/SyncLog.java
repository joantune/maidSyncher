package pt.ist.maidSyncher.domain.dsi;

import org.joda.time.LocalTime;

public class SyncLog extends SyncLog_Base {

    public SyncLog() {
        super();
        setSyncStartTime(new LocalTime());
        setSuccess(Boolean.FALSE);
    }

}
