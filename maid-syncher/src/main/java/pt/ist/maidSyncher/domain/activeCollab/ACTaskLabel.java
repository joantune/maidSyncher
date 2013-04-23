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
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACTaskLabel extends ACTaskLabel_Base {

    public ACTaskLabel() {
        super();
        setAcInstance(MaidRoot.getInstance().getAcInstance());
    }


    public static ACTaskLabel process(pt.ist.maidSyncher.api.activeCollab.ACTaskLabel acTaskLabel) {
        checkNotNull(acTaskLabel);

        MaidRoot instance = MaidRoot.getInstance();
        return (ACTaskLabel) findOrCreateAndProccess(acTaskLabel, ACTaskLabel.class, instance.getAcObjects());
    }

    public static ACTaskLabel findById(long id) {
        return (ACTaskLabel) MiscUtils.findACObjectsById(id, ACTaskLabel.class);
    }



    @Override
    protected DSIObject getDSIObject() {
        return null;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null; //task labels, [i.e., INPROGRESS, DONE] etc, are not synched.
        //only the Issues are based on their value
    }

}
