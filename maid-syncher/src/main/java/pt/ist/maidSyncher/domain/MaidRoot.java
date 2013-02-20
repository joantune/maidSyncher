package pt.ist.maidSyncher.domain;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.maidSyncher.domain.activeCollab.ACInstance;
import pt.ist.maidSyncher.domain.github.GHOrganization;

public class MaidRoot extends MaidRoot_Base {

    public static MaidRoot getInstance() {
        return FenixFramework.getRoot();
    }

    public MaidRoot() {
        super();
        checkIfIsSingleton();
        init();
    }

    public void init() {
        GHOrganization ghOrganization = getGhOrganization();
        if (ghOrganization == null)
            setGhOrganization(new GHOrganization());
        ACInstance acInstance = getAcInstance();
        if (acInstance == null)
            setAcInstance(new ACInstance());

    }

    private void checkIfIsSingleton() {
        if (FenixFramework.getRoot() != null && FenixFramework.getRoot() != this) {
            throw new Error("There can only be one! (instance of MyOrg)");
        }
    }

//    public void fullySync(User repository) {
//        checkNotNull(repository, "repository must not be null");
//        checkArgument(repository.getType().equals(User.TYPE_ORG), "You must provide a repository");
//
//    }


}
