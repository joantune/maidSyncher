package pt.ist.maidSyncher.domain.sync.logs;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.LocalTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.maidSyncher.domain.MaidRoot;

public class SyncLog extends SyncLog_Base {

    public SyncLog() {
        super();
        setMaidRoot(MaidRoot.getInstance());
        setSyncStartTime(new LocalTime());
        markAsFailure();
    }

    public static final String STATUS_CONFLICT = "Conflict";
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_FAILURE = "Failure";

    public void markAsFailure() {
        setStatus(STATUS_FAILURE);
    }

    public void markAsSuccess() {
        setStatus(STATUS_SUCCESS);
    }

    public void markAsConflict() {
        setStatus(STATUS_CONFLICT);
    }

    @Atomic(mode = TxMode.WRITE)
    public void registerExceptionAndMarkAsFailed(Exception ex) {
        setSyncEndTime(new LocalTime());
        setSerializedStackTrace(ExceptionUtils.getStackTrace(ex));
        markAsFailure();

    }

}
