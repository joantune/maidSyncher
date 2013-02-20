package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.egit.github.core.Repository;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;

public class GHRepository extends GHRepository_Base {

    public  GHRepository() {
        super();
        MaidRoot.getInstance().addGhRepositories(this);
        //the organization of this repository should always be the implicit one
        setOrganization(MaidRoot.getInstance().getGhOrganization());
    }

    @Service
    public static GHRepository process(Repository repository) {
        checkNotNull(repository);
        MaidRoot maidRoot = MaidRoot.getInstance();
        return (GHRepository) findOrCreateAndProccess(repository, GHRepository.class, maidRoot.getGhRepositories());
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync) {
        // TODO Auto-generated method stub

    }

}
