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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;

public abstract class GHObject extends GHObject_Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(GHObject.class);

    public static final String DSC_CREATED_AT = "createdAt";

    public GHObject() {
        super();
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null;
    }

}
