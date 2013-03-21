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


public abstract class ACGeneralTask extends ACGeneralTask_Base {
    //List of descriptor names
    protected static final String DSC_NAME = "name";
    protected static final String DSC_PRIORITY = "priority";
    protected static final String DSC_DUE_ON = "dueOn";
    protected static final String DSC_COMPLETE = "complete";

    public  ACGeneralTask() {
        super();
    }




}
