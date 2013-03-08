package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.joda.time.LocalTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class GHLabel extends GHLabel_Base {

    public  GHLabel() {
        super();
        MaidRoot.getInstance().addGhLabels(this);
    }

    static GHLabel process(Label label) {
        checkNotNull(label);
        MaidRoot maidRoot = MaidRoot.getInstance();
        GHLabel ghLabel =
                (GHLabel) findOrCreateAndProccess(label, GHLabel.class, maidRoot.getGhLabels(), ObjectFindStrategy.FIND_BY_URL);
        return ghLabel;
    }

    @Service
    public static GHLabel process(Label label, Repository repository) {
        checkNotNull(label);

        //each label belongs to a given repository

        //let's get a label first
        GHLabel ghLabel = process(label);

        //now the rep
        GHRepository ghRepository = GHRepository.process(repository);
        ghLabel.setRepository(ghRepository);
        return ghLabel;
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        /*we have no updated at filed (which is no big deal, so, let's make
         * this have less priority [either return the creation date or
         * the date of the last time it was synched] */
        return getLastSynchTime() == null ? getCreatedAt() : getLastSynchTime();
    }

    @Override
    protected DSIObject getDSIObject() {
        return getDsiObjectProject();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        //not all of these kinds of objects have such a relation
        //TODO
        return null;
    }

}
