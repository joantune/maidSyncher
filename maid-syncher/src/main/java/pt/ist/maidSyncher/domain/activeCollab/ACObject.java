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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.joda.time.DateTime;

import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;


public abstract class ACObject extends ACObject_Base {

    // The fields, to be used in PropertyDescriptor operations
    protected static final String DSC_CREATED_ON = "createdOn";
    protected static final String DSC_CREATED_BY_ID = "createdById";
    protected static final String DSC_UPDATED_ON = "updatedOn";
    protected static final String DSC_UPDATED_BY_ID = "updatedById";
    public static final String DSC_PERMALINK = "permalink";

    public  ACObject() {
        super();
        if (MaidRoot.getInstance() != null)
            MaidRoot.getInstance().addAcObjects(this);
    }

    @Override
    public DateTime getUpdatedAtDate() {
        if (getUpdatedOn() == null)
            return getCreatedOn();
        else
            return getUpdatedOn();
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        return null;
    }

    @Override
    public Collection<String> copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException, TaskNotVisibleException {
        Collection<String> copyPropertiesFrom = super.copyPropertiesFrom(orig);
        //let's use the permalink that should already be in place, to use it to the htmlUrl;
        setHtmlUrl(getPermalink());
        return copyPropertiesFrom;
    }


}
