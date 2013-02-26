package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.List;

import jvstm.cps.ConsistencyPredicate;

import org.eclipse.egit.github.core.User;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;

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
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }


}
