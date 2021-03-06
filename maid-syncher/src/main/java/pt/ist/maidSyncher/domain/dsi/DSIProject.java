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

import pt.ist.maidSyncher.domain.github.GHLabel;
import pt.ist.maidSyncher.domain.github.GHRepository;

public class DSIProject extends DSIProject_Base {

    public  DSIProject() {
        super();
    }

    public GHLabel getGitHubLabelFor(GHRepository ghRepository) {
        for (GHLabel ghLabel : getGitHubLabelsSet()) {
            if (ObjectUtils.equals(ghLabel.getRepository(), ghRepository)) {
                return ghLabel;
            }
        }
        return null;
    }

}
