package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.IllegalWriteException;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class ACInstance extends ACInstance_Base {
    private static final Logger LOGGER = LoggerFactory.getLogger(ACInstance.class);

    public ACInstance() {
        super();
        MaidRoot maidRoot = MaidRoot.getInstance();
        if (maidRoot != null) {
            maidRoot.setAcInstance(this);
        }
    }

    public ACInstance(MaidRoot maidRoot) {
        maidRoot.setAcInstance(this);
        maidRoot.addAcObjects(this);
    }

    @Service
    public static ACInstance process(pt.ist.maidSyncher.api.activeCollab.ACInstance acInstance) {
        checkNotNull(acInstance);
        ACInstance instance = MaidRoot.getInstance().getAcInstance();
        try {
            if (instance != null) {
                instance.copyPropertiesFrom(acInstance);
                return instance;
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            if (e.getCause() instanceof IllegalWriteException)
                throw (IllegalWriteException) e.getCause();
            LOGGER.error("error copying properties for ACInstance, creating a new one", e);
        }
        return (ACInstance) findOrCreateAndProccess(acInstance, ACInstance.class,
                Collections.singleton(MaidRoot.getInstance().getAcInstance()));
    }


    @Override
    protected DSIObject getDSIObject() {
        return null;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        throw new UnsupportedOperationException();
    }

}
