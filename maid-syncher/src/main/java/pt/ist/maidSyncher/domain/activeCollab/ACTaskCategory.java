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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pt.ist.maidSyncher.api.activeCollab.ACCategory;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.utils.MiscUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ACTaskCategory extends ACTaskCategory_Base {

    public final static String REPOSITORY_PREFIX = "R-";

    public  ACTaskCategory() {
        super();
    }

    private static ACTaskCategory process(ACCategory category) {
        checkNotNull(category);
        return (ACTaskCategory) findOrCreateAndProccess(category, ACTaskCategory.class, MaidRoot.getInstance().getAcObjectsSet());
    }

    public static ACTaskCategory process(ACCategory category, long projectId, boolean skipSync) {
        checkNotNull(category);
        ACTaskCategory newlyCreatedOrRetrievedACTaskCategory =
                (ACTaskCategory) findOrCreateAndProccess(category, ACTaskCategory.class,
                        MaidRoot.getInstance().getAcObjectsSet(),
                        skipSync);
        pt.ist.maidSyncher.domain.activeCollab.ACProject acProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.findById(projectId);
        acProject.addTaskCategoriesDefined(newlyCreatedOrRetrievedACTaskCategory);
        return newlyCreatedOrRetrievedACTaskCategory;

    }

    public static void process(Set<ACCategory> categories, ACProject project) {
        checkNotNull(project);
        //let's get the project
        pt.ist.maidSyncher.domain.activeCollab.ACProject acProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);

        //let's take care of each category
        Set<ACTaskCategory> oldTaskCategoriesDefined = new HashSet<ACTaskCategory>(acProject.getTaskCategoriesDefinedSet());
        Set<ACTaskCategory> newTaskCategoriesDefined = new HashSet<ACTaskCategory>();

        for (ACCategory category : categories) {
            ACTaskCategory acTaskCategory = process(category);
            newTaskCategoriesDefined.add(acTaskCategory);
        }

        //let's remove all of them
        acProject.getTaskCategoriesDefinedSet().clear();
        acProject.getTaskCategoriesDefinedSet().addAll(newTaskCategoriesDefined);

        //let's trigger the DELETE events needed
        oldTaskCategoriesDefined.removeAll(newTaskCategoriesDefined);
        for (ACTaskCategory removedTaskCategory : oldTaskCategoriesDefined) {
            SyncEvent.createAndAddADeleteEventWithoutAPIObj(removedTaskCategory);
        }


    }



    public static ACTaskCategory findById(long id) {
        return (ACTaskCategory) MiscUtils.findACObjectsById(id, ACTaskCategory.class);
    }

    public static Collection<ACTaskCategory> getByName(final String name) {
        checkArgument(StringUtils.isBlank(name) == false, "Name mustn't be blank");
        MaidRoot maidRoot = MaidRoot.getInstance();

        Collection<ACObject> existingACTaskCategories =
                Collections2.filter(maidRoot.getAcObjectsSet(), new Predicate<ACObject>() {
                    @Override
                    public boolean apply(ACObject acObject) {
                        if (acObject == null)
                            return false;
                        if (acObject instanceof ACTaskCategory) {
                            ACTaskCategory acTaskCategory = (ACTaskCategory) acObject;
                            if (acTaskCategory.getName().equalsIgnoreCase(name))
                                return true;
                            return false;
                        } else
                            return false;

                    }
                });
        return Collections2.transform(existingACTaskCategories, new Function<ACObject, ACTaskCategory>() {
            @Override
            public ACTaskCategory apply(ACObject acObject) {
                return (ACTaskCategory) acObject;
            }
        });


    }

    public static boolean hasGHSide(ACTaskCategory acTaskCategory) {
        if (acTaskCategory == null)
            return false;
        else
            return acTaskCategory.hasGHSide();

    }

    public boolean hasGHSide() {
        if (getDSIObject() == null || ((DSIRepository) getDSIObject()).getGitHubRepository() == null)
            return false;
        return true;
    }



    @Override
    public DSIObject getDSIObject() {
        return getDsiObjectRepository();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
//        DSIObject dsiObject = getDSIObject();
//        if (dsiObject == null) {
//            dsiObject = new DSIRepository();
//            setDsiObjectRepository((DSIRepository) dsiObject);
//        }
//        return dsiObject;
        //it will be done by the Synching of the GHRepository
        throw new UnsupportedOperationException();
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null; //while ACTaskCategories are repositories on GH
        //they do not trigger any sync by themselves
        //TODO enforce the original name, if it has changed issue #7
    }


}
