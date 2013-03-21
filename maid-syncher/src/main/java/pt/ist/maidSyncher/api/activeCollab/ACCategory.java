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
/**
 * 
 */
package pt.ist.maidSyncher.api.activeCollab;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.json.simple.JSONObject;

import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 13 de Fev de 2013
 * 
 *         It represents an Active Collab category
 */
public class ACCategory extends ACObject {

    private String name;

    private long projectId;

    private final static String PROJECT_CLASS = "Project";

    public ACCategory(JSONObject jsonObject) throws IOException {
        super(jsonObject);

    }

    public ACCategory(long categoryId, long projectId, String name) {
        checkArgument(categoryId > 0);
        checkArgument(projectId > 0);
        checkNotNull(name);
        this._id = categoryId;
        this.setProjectId(projectId);
        this.name = name;
    }

    public ACCategory() {
        super();
    }

    @Override
    protected void init(JSONObject jsonObject) throws IOException {
        setName(JsonRest.getString(jsonObject, "name"));
        String parentClass = JsonRest.getString(jsonObject, "parent_class");
        if (parentClass.equals(PROJECT_CLASS)) {
            setProjectId(JsonRest.getInt(jsonObject, "parent_id"));
        }

    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.ACObject#toJSONString()
     */
    @Override
    public String toJSONString() {
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "category[name]", name);
        return postData.toString();
    }

    public String getName() {
        return name;
    }


    public ACCategory update() throws IOException {
        //let us construct the URL and send an edit
        String path = ACContext.getBasicUrlForPath("projects/" + this.getProjectId() + "/tasks/categories/" + this._id + "/edit");
        return new ACCategory(ACCategory.postObject(path, toJSONString()));

    }

    /**
     * 
     * @param acCategoryToCreate
     * @param projectId
     * @param clazz the class (on the *.domain package), of the
     *            kind of category
     * @return
     * @throws IOException
     */
    public static ACCategory create(ACCategory acCategoryToCreate, long projectId,
            Class<? extends pt.ist.maidSyncher.domain.activeCollab.ACObject> clazz)
                    throws IOException {
        checkNotNull(acCategoryToCreate);
        checkNotNull(clazz);
        checkArgument(ACTaskCategory.class.equals(clazz),
                "currently we don't support creation of other categories, only ACTaskCategory");
        String path = null;
        if (ACTaskCategory.class.equals(clazz)) {
            path = ACContext.getBasicUrlForPath("projects/" + projectId + "/tasks/categories");

        }
        else
            return null;

        return new ACCategory(postObject(path, acCategoryToCreate.toJSONString()));
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

}
