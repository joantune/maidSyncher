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

import java.util.Collections;

import org.eclipse.egit.github.core.User;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;

public class GHOrganization extends GHOrganization_Base {

    public GHOrganization() {
        MaidRoot rootInstance = MaidRoot.getInstance();
        if (rootInstance != null) {

            GHOrganization previousOrganization = rootInstance.getGhOrganization();
            rootInstance.setGhOrganization(this);
            if (previousOrganization != null) {
                previousOrganization.deleteDomainObject();
            }
        }

    }

    @Service
    public static GHOrganization process(User user) {
        checkNotNull(user);
        checkArgument(user.getType().equals(User.TYPE_ORG), "you must provide a User of the type Organizaiton");
        MaidRoot maidRoot = MaidRoot.getInstance();

        return (GHOrganization) findOrCreateAndProccess(user, GHOrganization.class,
                Collections.singleton(maidRoot.getGhOrganization()));

    }

}
