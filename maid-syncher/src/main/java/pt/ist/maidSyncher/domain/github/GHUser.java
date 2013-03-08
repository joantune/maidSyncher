package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import jvstm.cps.ConsistencyPredicate;

import org.eclipse.egit.github.core.User;
import org.joda.time.LocalTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIUser;

public class GHUser extends GHUser_Base {

    public  GHUser() {
        super();
        if (!(this instanceof GHOrganization))
            MaidRoot.getInstance().addGhUsers(this);
    }

    @Service
    public static GHUser process(User user) {
        checkNotNull(user);
        checkArgument(user.getType().equals(User.TYPE_USER), "provided user must be of type user");
        List<GHUser> ghUsers = MaidRoot.getInstance().getGhUsers();
        return (GHUser) findOrCreateAndProccess(user, GHUser.class, ghUsers);

    }

    @ConsistencyPredicate
    protected boolean checkMultiplicityOfOrganization() {
        if (this instanceof GHOrganization)
            return true;
        else {
            return hasOrganization();
        }

    }


    @ConsistencyPredicate
    protected boolean checkHasRelationWithMaidRoot() {
        if (this instanceof GHOrganization) {
            return ((GHOrganization) this).hasMaidRootFromOrg();
        } else {
            return hasMaidRootFromUser();
        }
    }

    @Override
    protected DSIObject getDSIObject() {
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

}
