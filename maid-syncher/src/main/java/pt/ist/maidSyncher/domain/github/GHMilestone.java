package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;

public class GHMilestone extends GHMilestone_Base {

    public GHMilestone() {
        super();
        MaidRoot.getInstance().addGhMilestones(this);
    }

    static GHMilestone process(Milestone milestone) {
        checkNotNull(milestone);
        MaidRoot maidRoot = MaidRoot.getInstance();

        GHMilestone ghMilestone =
                (GHMilestone) findOrCreateAndProccess(milestone, GHMilestone.class, maidRoot.getGhMilestones(),
                        ObjectFindStrategy.FIND_BY_URL);
        return ghMilestone;
    }

    @Service
    public static GHMilestone process(Milestone milestone, Repository repository) {
        MaidRoot maidRoot = MaidRoot.getInstance();

        GHMilestone ghMilestone = process(milestone);

        //now let's take care of the repository
        GHRepository ghRepository = GHRepository.process(repository);
        ghMilestone.setRepository(ghRepository);

        return ghMilestone;
    }

    @Override
    public void sync(Object objectThatTriggeredTheSync) {
        // TODO Auto-generated method stub

    }

}
