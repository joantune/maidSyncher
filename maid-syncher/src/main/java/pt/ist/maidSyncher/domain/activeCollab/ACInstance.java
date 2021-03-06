/*******************************************************************************
 * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Luis Silva - ACGHSync
 *     João Antunes - initial API and implementation
 ******************************************************************************/
package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.core.WriteOnReadError;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

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

    public static ACInstance process(pt.ist.maidSyncher.api.activeCollab.ACInstance acInstance) {
        checkNotNull(acInstance);
        ACInstance instance = MaidRoot.getInstance().getAcInstance();
        try {
            if (instance != null) {
                instance.copyPropertiesFrom(acInstance);
                return instance;
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            if (e.getCause() instanceof WriteOnReadError)
                throw (WriteOnReadError) e.getCause();
            LOGGER.error("error copying properties for ACInstance, creating a new one", e);
        }
        return (ACInstance) findOrCreateAndProccess(acInstance, ACInstance.class,
                Collections.singleton(MaidRoot.getInstance().getAcInstance()));
    }


    @Override
    public DSIObject getDSIObject() {
        return null;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null;
    }

}
