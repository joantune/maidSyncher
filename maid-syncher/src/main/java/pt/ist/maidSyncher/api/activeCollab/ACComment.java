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
package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

public class ACComment extends ACObject {

    // attributes
    private String body;
    private long parentId;
    private String parentClass;

    public ACComment() {
        super();
    }

    public ACComment(JSONObject jsonObj) throws IOException {
        super(jsonObj);
        JSONObject jsonObjToConsider = null;
        String commentBody = JsonRest.getString(jsonObj, "body");
        if (commentBody == null && StringUtils.equalsIgnoreCase(JsonRest.getString(jsonObj, "parent_class"), "Task")) {
            //we have only the reference, we should make a new request to get the content
            jsonObjToConsider = (JSONObject) getRequestProcessor().processGet(getUrl());

        } else
            jsonObjToConsider = jsonObj;
        init(jsonObjToConsider);

    }

    @Override
    protected void init(JSONObject jsonObj) {
        setBody(JsonRest.getString(jsonObj, "body"));
        setParentId(JsonRest.getInt(jsonObj, "parent_id"));
        setParentClass(JsonRest.getString(jsonObj, "parent_class"));
    }

    @Override
    public String toJSONString() {
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "comment[body]", getBody());
        return postData.toString();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getParentClass() {
        return parentClass;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }
}
