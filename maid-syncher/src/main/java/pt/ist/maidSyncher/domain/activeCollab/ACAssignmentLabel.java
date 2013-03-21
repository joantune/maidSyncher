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

import pt.ist.maidSyncher.domain.dsi.DSIObject;


public class ACAssignmentLabel extends ACAssignmentLabel_Base {

    public  ACAssignmentLabel() {
        super();
    }


    @Override
    protected DSIObject getDSIObject() {
        return null;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        throw new UnsupportedOperationException();
    }

}
