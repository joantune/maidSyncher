package pt.ist.maidSyncher.domain.sync.logs;

import static com.google.common.base.Preconditions.checkNotNull;

public class SyncWarningLog extends SyncWarningLog_Base {

    public SyncWarningLog(SyncLog syncLog, String description) {
        super();
        checkNotNull(syncLog);
        checkNotNull(description);
        setDescription(description);
        setSyncLog(syncLog);
    }

}
