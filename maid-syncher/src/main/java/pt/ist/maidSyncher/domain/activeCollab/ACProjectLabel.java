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
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

public class ACProjectLabel extends ACProjectLabel_Base {

    public  ACProjectLabel() {
        super();
        setAcInstance(MaidRoot.getInstance().getAcInstance());
    }

    public static ACProjectLabel process(pt.ist.maidSyncher.api.activeCollab.ACProjectLabel acProjectLabel)
    {
        checkNotNull(acProjectLabel);
        MaidRoot maidRoot = MaidRoot.getInstance();

        return (ACProjectLabel) findOrCreateAndProccess(acProjectLabel, ACProjectLabel.class, maidRoot.getAcObjectsSet());
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
        return null; //they have no specific object on the GH side that changes when
        //this object changes
    }

}
