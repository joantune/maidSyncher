package pt.ist.maidSyncher.domain.sync.logs;

import pt.ist.maidSyncher.domain.sync.SyncEvent;

public class SyncEventConflictLog extends SyncEventConflictLog_Base {

    public SyncEventConflictLog() {
        super();
    }

    public SyncEventConflictLog(SyncEvent syncEvent, SyncEvent syncEventAlreadyPresent) {
        setEventOneOriginator(syncEvent.getOriginObject());
        setEventTwoOriginator(syncEventAlreadyPresent.getOriginObject());

        setEventOneTypeOfChangeEvent(syncEvent.getTypeOfChangeEvent());
        setEventTwoTypeOfChangeEvent(syncEventAlreadyPresent.getTypeOfChangeEvent());
    }

    public void markSecondAsWinner() {
        setWinnerObject(getEventTwoOriginator());
    }

    public void markFirstAsWinner() {
        setWinnerObject(getEventOneOriginator());
    }

}
