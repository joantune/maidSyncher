package pt.ist.maidSyncher.domain.sync.logs;

import static com.google.common.base.Preconditions.checkNotNull;

import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class SyncActionLog extends SyncActionLog_Base {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncActionLog.class);

    public SyncActionLog(SyncLog associatedSyncLog, String urlOriginObject, DSIObject syncDSIObject) {
        super();
        checkNotNull(syncDSIObject);
        checkNotNull(associatedSyncLog);
        setSyncLog(associatedSyncLog);
        setSuccess(Boolean.FALSE);
        setUrlOriginObject(urlOriginObject);
        setDsiObject(syncDSIObject);
    }


    public void markStartOfSync() {
        setSyncStartTime(new LocalTime());
    }

    public void markEndOfSync(Boolean success, String... errorStrings) {
        checkNotNull(success);
        setSyncEndTime(new LocalTime());
        setSuccess(success);
        if (success.equals(Boolean.FALSE)) {
            //let's get the optional String that is the errorDescription
            if (errorStrings.length >= 1) {
                setErrorDescription(errorStrings[0]);
            }

        }
    }

}
