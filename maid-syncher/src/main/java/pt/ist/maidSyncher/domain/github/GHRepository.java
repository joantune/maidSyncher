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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.api.activeCollab.ACCategory;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.activeCollab.ACInstance;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.exceptions.SyncActionError;
import pt.ist.maidSyncher.domain.exceptions.SyncEventIncogruenceBetweenOriginAndDestination;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GHRepository extends GHRepository_Base implements IRepositoryIdProvider {

    public GHRepository() {
        super();
        MaidRoot.getInstance().addGhRepositories(this);
        //the organization of this repository should always be the implicit one
        setOrganization(MaidRoot.getInstance().getGhOrganization());
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GHRepository.class);

    //PropertyDescriptor s names:
    protected static final String DSC_OWNER = "owner";
    protected static final String DSC_HAS_DOWNLOADS = "hasDownloads";
    protected static final String DSC_GITURL = "gitUrl";
    protected static final String DSC_HAS_ISSUES = "hasIssues";
    protected static final String DSC_MASTER_BRANCH = "masterBranch";
    protected static final String DSC_DESCRIPTION = "description";
    protected static final String DSC_HAS_WIKI = "hasWiki";
    protected static final String DSC_PUSHED_AT = "pushedAt";
    protected static final String DSC_UPDATED_AT = "updatedAt";
    protected static final String DSC_NAME = "name";
    protected static final String DSC_OPEN_ISSUES = "openIssues";
    protected static final String DSC_SSH_URL = "sshUrl";
    protected static final String DSC_SVN_URL = "svnUrl";
    protected static final String DSC_CLONE_URL = "cloneUrl";
    protected static final String DSC_HTML_URL = "htmlUrl";

    @Override
    public Collection<String> copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException, TaskNotVisibleException {
        Set<String> changedPropertyDescriptors = new HashSet();
        changedPropertyDescriptors.addAll(super.copyPropertiesFrom(orig));
        //let's take care of the Owner
        Repository repository = (Repository) orig;
        User owner = repository.getOwner();
        if (owner.getType().equals(User.TYPE_USER)) {
            GHUser ghUser = GHUser.process(owner);
            GHUser oldOwner = getOwner();
            setOwner(ghUser);
            if (ObjectUtils.equals(ghUser, oldOwner) == false)
                changedPropertyDescriptors.add(getPropertyDescriptorNameAndCheckItExists(orig, DSC_OWNER));

        } else {
            GHOrganization ghOrg = GHOrganization.process(owner);
            GHUser oldOwner = getOwner();
            setOwner(ghOrg);
            if (ObjectUtils.equals(ghOrg, oldOwner) == false)
                changedPropertyDescriptors.add(getPropertyDescriptorNameAndCheckItExists(orig, DSC_OWNER));

        }
        return changedPropertyDescriptors;
    }

    public static GHRepository process(Repository repository, boolean skipSync) {
        checkNotNull(repository);
        MaidRoot maidRoot = MaidRoot.getInstance();
        return (GHRepository) findOrCreateAndProccess(repository, GHRepository.class, maidRoot.getGhRepositoriesSet(), skipSync);
    }

    public static GHRepository process(Repository repository) {
        return process(repository, false);
    }

    @Override
    public DSIObject getDSIObject() {
        return super.getDsiObjectRepository();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null) {
            dsiObject = new DSIRepository();//let's take care of the
            //default project and the task category on the sync
            setDsiObjectRepository((DSIRepository) dsiObject);
        }
        return dsiObject;
    }

    @Override
    public DateTime getUpdatedAtDate() {
        return getUpdatedAt() == null ? getCreatedAt() : getUpdatedAt();
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        //every GHRepository should have a default ACProject
        //and a ACTaskCategory.
        SyncActionWrapper syncActionWrapperToReturn = null;
        if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
            //then we should need to create an ACProject (if it doesn't exist)
            //and/or a ACTaskCategory (also if it doesn't exist)

            //validate the SyncUniverse
            if (syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.GITHUB))
                throw new SyncEventIncogruenceBetweenOriginAndDestination("For syncEvent: " + syncEvent.toString());

            syncActionWrapperToReturn = syncCreateEvent(syncEvent);

        } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
            //let's retrieve the default project, and the task category:
            DSIRepository dsiRepository = (DSIRepository) getDSIObject();
            Collection<ACTaskCategory> acTaskCategories = dsiRepository.getAcTaskCategoriesSet();
            pt.ist.maidSyncher.domain.activeCollab.ACProject defaultACProject = dsiRepository.getDefaultProject();

            syncActionWrapperToReturn = syncUpdateEvent(defaultACProject, acTaskCategories, syncEvent);

        } else {
            LOGGER.warn("Read and Delete events not supported yet. " + syncEvent);
            syncActionWrapperToReturn = new EmptySyncActionWrapper(syncEvent);
        }

        return syncActionWrapperToReturn;
    }

    private SyncActionWrapper syncUpdateEvent(pt.ist.maidSyncher.domain.activeCollab.ACProject defaultACProject,
            Collection<ACTaskCategory> acTaskCategories, final SyncEvent syncEvent) {
        //both (project and task category) ought to exist,
        //let's be conservative and throw an error if they don't
        checkNotNull(defaultACProject);
        checkNotNull(acTaskCategories);

        ACProject acProjectAux = null;

        final DSIRepository dsiRepository = (DSIRepository) getDSIObject();

        final Set<ACCategory> acCategoriesToEdit = new HashSet<ACCategory>();
        final Set<String> tickedDescriptors = new HashSet<>();

        for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor) {
            case DSC_ID:
            case DSC_OWNER:
            case DSC_HAS_DOWNLOADS:
            case DSC_GITURL:
            case DSC_URL:
            case DSC_HAS_ISSUES:
            case DSC_MASTER_BRANCH:
            case DSC_HAS_WIKI:
            case DSC_PUSHED_AT:
            case DSC_UPDATED_AT:
            case DSC_OPEN_ISSUES:
            case DSC_SSH_URL:
            case DSC_SVN_URL:
            case DSC_CLONE_URL:
            case DSC_CREATED_AT:
            case DSC_HTML_URL:
            case DSC_DESCRIPTION:
                //ˆˆ the ones that we don't have to do anything
//                //then we need to do something with the default project's description NOT!
//                if (acProjectAux == null) {
//                    acProjectAux = new ACProject();
//                }
//                acProjectAux = preFillProjectIfNeeded(defaultACProject, acProjectAux);
//                acProjectAux.set
                break;
            case DSC_NAME:
                //then we must change the name of the default project, and of all of the acTaskCategories
                acProjectAux = preFillProjectIfNeeded(defaultACProject, acProjectAux);
                acProjectAux.setName(getName());
                //let's take care of the categories
                for (ACTaskCategory acTaskCategory : dsiRepository.getAcTaskCategoriesSet()) {
                    acCategoriesToEdit.add(new ACCategory(acTaskCategory.getId(), acTaskCategory.getProject().getId(),
                            ACTaskCategory.REPOSITORY_PREFIX + getName()));
                }

                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        final ACProject acProjectToEdit = acProjectAux;

        return new SyncActionWrapper<SynchableObject>() {

            @Override
            public Set<SynchableObject> sync() throws SyncActionError {
                Set<SynchableObject> changedObjects = new HashSet<SynchableObject>();

                Set<ACTaskCategory> newACTaskCategories = new HashSet<>();

                try {
                    //let's take care of:

                    //. the project if we need to;
                    if (acProjectToEdit != null) {
                        ACProject newlyCreatedACProject = acProjectToEdit.update();
                        pt.ist.maidSyncher.domain.activeCollab.ACProject newACProject =
                                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(newlyCreatedACProject, true);
                        dsiRepository.setDefaultProject(newACProject);
                        changedObjects.add(newACProject);
                    }

                    //. the categories if we need to;
                    if (acCategoriesToEdit.isEmpty() == false) {
                        for (ACCategory acCategoryToEdit : acCategoriesToEdit) {
                            ACCategory newACCategory = acCategoryToEdit.update();
                            ACTaskCategory newACTaskCategory =
                                    ACTaskCategory.process(newACCategory, newACCategory.getProjectId(), true);
                            newACTaskCategories.add(newACTaskCategory);
                            changedObjects.add(newACTaskCategory);
                        }
                        //let's set the new task categories

                        //remove the old ones
                        for (ACTaskCategory oldAcTaskCategory : dsiRepository.getAcTaskCategoriesSet()) {
                            dsiRepository.removeAcTaskCategories(oldAcTaskCategory);
                        }

                        for (ACTaskCategory acTaskCategory : newACTaskCategories) {
                            dsiRepository.addAcTaskCategories(acTaskCategory);
                        }

                    }
                } catch (IOException ex) {
                    throw new SyncActionError(ex, changedObjects);
                }

                return changedObjects;
            }

            @Override
            public SyncEvent getOriginatingSyncEvent() {
                return syncEvent;
            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                return Collections.singleton((DSIObject) dsiRepository.getProject());
            }

            @Override
            public Set<Class> getSyncDependedTypesOfDSIObjects() {
                return Collections.singleton((Class) DSIProject.class);
            }

            @Override
            public Collection<String> getPropertyDescriptorNamesTicked() {
                return tickedDescriptors;
            }
        };
    }

    private ACProject preFillProjectIfNeeded(pt.ist.maidSyncher.domain.activeCollab.ACProject existingACProject,
            ACProject acApiProject) {
        ACProject acProjectToReturn;
        if (acApiProject != null) {
            acProjectToReturn = acApiProject;
        } else {
            acProjectToReturn = new ACProject();
        }

        try {
            existingACProject.copyPropertiesTo(acProjectToReturn);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
            LOGGER.warn("Trying to 'copyPropertiesTo' between domain.ACProject and api.ACProject", e);
            throw new Error("Trying to 'copyPropertiesTo' between domain.ACProject and api.ACProject", e);
        }
        return acProjectToReturn;
    }

    // owner hasDownloads gitUrl hasIssues masterBranch description hasWiki pushedAt updatedAt name openIssues sshUrl svnUrl cloneUrl createdAt htmlUrl
//    private SyncActionWrapper syncCreateEvent(final pt.ist.maidSyncher.domain.activeCollab.ACProject acExistingProject,
//            final Collection<ACTaskCategory> existingACTaskCategories, final SyncEvent syncEvent) {
    private SyncActionWrapper syncCreateEvent(final SyncEvent syncEvent) {
        final Set<String> tickedDescriptors = new HashSet<>();
        for (String changedDescriptor : syncEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor) {
            case DSC_ID:
            case DSC_OWNER:
            case DSC_HAS_DOWNLOADS:
            case DSC_URL:
            case DSC_GITURL:
            case DSC_HAS_ISSUES:
            case DSC_MASTER_BRANCH:
            case DSC_HAS_WIKI:
            case DSC_PUSHED_AT:
            case DSC_UPDATED_AT:
            case DSC_OPEN_ISSUES:
            case DSC_SSH_URL:
            case DSC_SVN_URL:
            case DSC_CLONE_URL:
            case DSC_CREATED_AT:
            case DSC_HTML_URL:
                //ˆˆthe ones that we don't have to do anything
            case DSC_DESCRIPTION:
                break;
            case DSC_NAME:
                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        return new SyncActionWrapper<SynchableObject>() {

            @Override
            public Set<SynchableObject> sync() throws SyncActionError {
                Set<SynchableObject> changedObjects = new HashSet<>();
                //let's try to see if an ACProject already exists or not
                pt.ist.maidSyncher.domain.activeCollab.ACProject acExistingProject =
                        pt.ist.maidSyncher.domain.activeCollab.ACProject.findByName(getName());

                //let's assert if we need to create the project
                pt.ist.maidSyncher.domain.activeCollab.ACProject acProjectToReturn = null;
                if (acExistingProject == null) {
                    //let us create it
                    ACProject newAcProject = new ACProject();
                    newAcProject.setName(getName());
                    ACInstance acInstance = MaidRoot.getInstance().getAcInstance();

                    newAcProject.setCompanyId((int) acInstance.getId());

                    //lets ignore the leadId, status, budget, and labels
                    try {
                        acProjectToReturn =
                                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(ACProject.create(newAcProject), true);
                        changedObjects.add(acProjectToReturn);
                    } catch (IOException exception) {
                        throw new SyncActionError(exception, changedObjects);
                    }
                }

                //let's gather all of the active ACProjects
                final Set<pt.ist.maidSyncher.domain.activeCollab.ACProject> activeProjects =
                        pt.ist.maidSyncher.domain.activeCollab.ACProject.getActiveProjects();
                //and for each, we should have an ACTaskCategory created or assigned

                DSIRepository dsiRepository = (DSIRepository) getDSIObject();
                dsiRepository.setDefaultProject(acProjectToReturn);

                //let us assert which ACTaskCategory we have to create
                //and which ones we should just simply apply
                Set<ACTaskCategory> acTaskCategoriesToAssign = new HashSet<>();
                Set<pt.ist.maidSyncher.domain.activeCollab.ACProject> projectsToCreateTaskCategoriesFor =
                        new HashSet<>(activeProjects);

                        Collection<ACTaskCategory> existingACTaskCategories =
                                ACTaskCategory.getByName(ACTaskCategory.REPOSITORY_PREFIX + getName());
                        for (ACTaskCategory existingCategory : existingACTaskCategories) {
                            if (existingCategory.getProject().getArchived() == false) {
                                projectsToCreateTaskCategoriesFor.remove(existingCategory.getProject());
                                acTaskCategoriesToAssign.add(existingCategory);
                            }
                        }

                        try {
                            //now, let us create the rest of the categories
                            for (pt.ist.maidSyncher.domain.activeCollab.ACProject acProjectToCreateTCategoryFor : projectsToCreateTaskCategoriesFor) {
                                //let's create it
                                ACCategory acNewCategory = new ACCategory();
                                acNewCategory.setName(ACTaskCategory.REPOSITORY_PREFIX + getName());
                                acNewCategory =
                                        ACCategory.create(acNewCategory, acProjectToCreateTCategoryFor.getId(), ACTaskCategory.class);
                                ACTaskCategory newlyCreatedACTaskCategory =
                                        ACTaskCategory.process(acNewCategory, acProjectToCreateTCategoryFor.getId(), true);
                                changedObjects.add(newlyCreatedACTaskCategory);
                                acTaskCategoriesToAssign.add(newlyCreatedACTaskCategory);
                            }
                        } catch (IOException exception) {
                            throw new SyncActionError(exception, changedObjects);
                        }
                        //now, let's remove the old ones, and add the new ones
                        dsiRepository.getAcTaskCategoriesSet().clear();
                        dsiRepository.getAcTaskCategoriesSet().addAll(acTaskCategoriesToAssign);

                        ArrayList<SynchableObject> synchedObjects = new ArrayList<SynchableObject>();
                        synchedObjects.add(acProjectToReturn);
                        synchedObjects.addAll(dsiRepository.getAcTaskCategoriesSet());

                        return changedObjects;
            }

            @Override
            public SyncEvent getOriginatingSyncEvent() {
                return syncEvent;
            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                return Collections.emptyList();
            }

            @Override
            public Set<Class> getSyncDependedTypesOfDSIObjects() {
                return Collections.emptySet();
            }

            @Override
            public Collection<String> getPropertyDescriptorNamesTicked() {
                return tickedDescriptors;
            }
        };
    }

    protected static GHRepository findById(final long id) {
        checkArgument(id > 0);
        Optional<GHRepository> optionalGHRepository =
                Iterables.tryFind(MaidRoot.getInstance().getGhRepositoriesSet(), new Predicate<GHRepository>() {
                    @Override
                    public boolean apply(GHRepository ghRepository) {
                        if (ghRepository == null)
                            return false;
                        return ghRepository.getId() == id;
                    }
                });
        return optionalGHRepository.isPresent() ? optionalGHRepository.get() : null;
    }

    @Override
    public String generateId() {
        if (getName() == null || getOwner() == null || getOwner().getLogin() == null)
            return null;
        return getOwner().getLogin() + "/" + getName();
    }
}
