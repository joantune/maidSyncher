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
import pt.ist.maidSyncher.domain.dsi.DSIUser;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACUser extends ACUser_Base {

    public  ACUser() {
        super();
    }

    public static ACUser process(pt.ist.maidSyncher.api.activeCollab.ACUser acUser) {
        checkNotNull(acUser);
        return (ACUser) findOrCreateAndProccess(acUser, ACUser.class, MaidRoot.getInstance().getAcObjectsSet());
    }

    public static ACUser findById(long id) {
        return (ACUser) MiscUtils.findACObjectsById(id, ACUser.class);
    }

    @Override
    protected DSIObject getDSIObject() {
        return getDsiObjectFromACUser();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null) {
            dsiObject = new DSIUser();
        }

        return dsiObject;
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null; //users don't need to be synched
    }

}
