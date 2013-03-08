package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.egit.github.core.Repository;
import org.joda.time.LocalTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;

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
    protected DSIObject getDSIObject() {
        return super.getDsiObjectRepository();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null)
            dsiObject = new DSIRepository();
        return dsiObject;
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        return getUpdatedAt() == null ? getCreatedAt() : getUpdatedAt();
    }

}
