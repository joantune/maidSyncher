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

import pt.ist.maidSyncher.domain.MaidRoot;

public class DSIObject extends DSIObject_Base {

    public  DSIObject() {
        super();
        setMaidRoot(MaidRoot.getInstance());
    }


}
