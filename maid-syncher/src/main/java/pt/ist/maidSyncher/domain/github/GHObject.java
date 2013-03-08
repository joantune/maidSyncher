package pt.ist.maidSyncher.domain.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.domain.SyncEvent;

public abstract class GHObject extends GHObject_Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(GHObject.class);

    public GHObject() {
        super();
    }

    @Override
    public void sync(SyncEvent syncEvent) {
        // TODO
    }
}
