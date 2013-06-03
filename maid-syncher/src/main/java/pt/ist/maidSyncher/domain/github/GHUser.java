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
package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import jvstm.cps.ConsistencyPredicate;

import org.eclipse.egit.github.core.User;
import org.joda.time.LocalTime;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIUser;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

public class GHUser extends GHUser_Base {

    public  GHUser() {
        super();
        if (!(this instanceof GHOrganization))
            MaidRoot.getInstance().addGhUsers(this);
    }

    public static GHUser process(User user) {
        checkNotNull(user);
        checkArgument(user.getType().equals(User.TYPE_USER),
                "provided user must be of type user, and it is of type: " + user.getType());
        Set<GHUser> ghUsers = MaidRoot.getInstance().getGhUsersSet();
        return (GHUser) findOrCreateAndProccess(user, GHUser.class, ghUsers);

    }

    @ConsistencyPredicate
    protected boolean checkMultiplicityOfOrganization() {
        if (this instanceof GHOrganization)
            return true;
        else {
            return getOrganization() != null;
        }

    }


    @ConsistencyPredicate
    protected boolean checkHasRelationWithMaidRoot() {
        if (this instanceof GHOrganization) {
            return ((GHOrganization) this).getMaidRootFromOrg() != null;
        } else {
            return getMaidRootFromUser() != null;
        }
    }

    @Override
    public DSIObject getDSIObject() {
        return getDsiObjectUser();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null)
            dsiObject = new DSIUser();
        return dsiObject;
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        /*we have no updated at filed (which is no big deal, so, let's make
         * this have less priority [either return the creation date or
         * the date of the last time it was synched] */
        return getLastSynchTime() == null ? getCreatedAt() : getLastSynchTime();
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null; //changes to this object does not update others in the
        //active collab universe
    }

}
