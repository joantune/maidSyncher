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
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACMilestone extends ACMilestone_Base {

    public  ACMilestone() {
        super();
    }

//    static ACMilestone process(pt.ist.maidSyncher.api.activeCollab.ACMilestone milestone) {
//        checkNotNull(milestone);
//        return (ACMilestone) findOrCreateAndProccess(milestone, ACMilestone.class, MaidRoot.getInstance().getAcObjects());
//    }

    @Service
    public static ACMilestone process(pt.ist.maidSyncher.api.activeCollab.ACMilestone milestone, ACProject project) {
        checkNotNull(milestone);
        ACMilestone acMilestone =
                (ACMilestone) findOrCreateAndProccess(milestone, ACMilestone.class, MaidRoot.getInstance().getAcObjects());
        pt.ist.maidSyncher.domain.activeCollab.ACProject acProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);
        acMilestone.setProject(acProject);
        return acMilestone;
    }

    public static ACMilestone findById(long id) {
        return (ACMilestone) MiscUtils.findACObjectsById(id, ACMilestone.class);
    }


    @Override
    protected DSIObject getDSIObject() {
        return getDsiObjectMilestone();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null)
            dsiObject = new DSIMilestone();
        return dsiObject;
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null;
    }

}
