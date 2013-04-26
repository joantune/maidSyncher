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

import org.apache.commons.lang.ObjectUtils;

import pt.ist.maidSyncher.domain.activeCollab.ACMilestone;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DSIMilestone extends DSIMilestone_Base {

    public  DSIMilestone() {
        super();
    }

    public ACMilestone getAcMilestone(final ACProject acProject) {
        Optional<ACMilestone> optionalMilestone = Iterables.tryFind(getAcMilestonesSet(), new Predicate<ACMilestone>() {
            @Override
            public boolean apply(ACMilestone input) {
                if(input == null)
                    return false;
                return ObjectUtils.equals(input.getProject(), acProject);
            }
        });
        return optionalMilestone.orNull();
    }

    public GHMilestone getGhMilestone(final GHRepository ghRepository) {
        Optional<GHMilestone> optionalMilestone = Iterables.tryFind(getGhMilestonesSet(), new Predicate<GHMilestone>() {
            @Override
            public boolean apply(GHMilestone input) {
                if (input == null)
                    return false;
                return ObjectUtils.equals(input.getRepository(), ghRepository);
            }
        });
        return optionalMilestone.orNull();
    }

}
