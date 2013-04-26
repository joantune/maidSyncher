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
package pt.ist.maidSyncher.domain.dsi;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang.ObjectUtils;

import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;

public class DSIRepository extends DSIRepository_Base {

    public  DSIRepository() {
        super();
    }

    public ACTaskCategory getACTaskCategoryFor(ACProject acProject) {
        checkNotNull(acProject);
        for (ACTaskCategory acTaskCategory : getAcTaskCategoriesSet()) {
            if (ObjectUtils.equals(acTaskCategory.getProject(), acProject))
                return acTaskCategory;

        }
        return null;

    }

}
