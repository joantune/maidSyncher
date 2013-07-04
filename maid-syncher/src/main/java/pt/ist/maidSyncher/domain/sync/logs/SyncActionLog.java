package pt.ist.maidSyncher.domain.sync.logs;

import static com.google.common.base.Preconditions.checkNotNull;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

public class SyncActionLog extends SyncActionLog_Base {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncActionLog.class);

    public SyncActionLog(SyncLog associatedSyncLog, SyncEvent syncEvent, DSIObject syncDSIObject) {
        super();
        SynchableObject originObject = syncEvent.getOriginObject();
        checkNotNull(syncDSIObject);
        checkNotNull(associatedSyncLog);
        setSyncLog(associatedSyncLog);
        setSuccess(Boolean.FALSE);
        setUrlOriginObject(originObject.getHtmlUrl());
        setTypeOriginObject(originObject.getClass().getName());
        setDsiObject(syncDSIObject);
        setChangedDescriptors(syncEvent.getChangedPropertyDescriptorNames());
    }


    public void markStartOfSync() {
        setSyncStartTime(new DateTime());
    }

    public void markEndOfSync(Boolean success, String... errorStrings) {
        checkNotNull(success);
        setSyncEndTime(new DateTime());
        setSuccess(success);
        if (success.equals(Boolean.FALSE)) {
            //let's get the optional String that is the errorDescription
            if (errorStrings.length >= 1) {
                setErrorDescription(errorStrings[0]);
            }

        }
    }

}
