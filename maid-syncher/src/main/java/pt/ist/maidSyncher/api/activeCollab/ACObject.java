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
import java.util.Date;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;

public abstract class ACObject {

    public static final Logger LOGGER = LoggerFactory.getLogger(ACObject.class);

    private long _id;
    protected String _url;
    protected Date _createdOn;
    protected long _createdById;
    protected Date _updatedOn;
    protected long _updatedById;

    public ACObject()
    {
        setId(-1);
        _createdById = -1;
        _updatedById = -1;
    }

    public ACObject(JSONObject jsonObj) throws IOException
    {
        privateInit(jsonObj);
        init(jsonObj);
    }

    protected abstract void init(JSONObject jsonObject) throws IOException;

    private void privateInit(JSONObject jsonObj) {
        setId(JsonRest.getInt(jsonObj, "id"));
        _url = JsonRest.getString(jsonObj, "permalink");
        if (_url != null)
            _url = _url.replaceFirst("public/index","api");

        LOGGER.trace("URL:" + _url);

        _createdOn = JsonRest.getDate(jsonObj, "created_on");
        _createdById = JsonRest.getInt(jsonObj, "created_by_id");
        _updatedOn = JsonRest.getDate(jsonObj, "updated_on");
        _updatedById = JsonRest.getInt(jsonObj, "updated_by_id");

    }


    protected final void updateFromPermalink() throws IOException {
        //let's use the permalink to get all of the information
        JSONObject jsonObject = (JSONObject) getRequestProcessor().processGet(_url);
        init(jsonObject);

    }

    private static RequestProcessor requestProcessor = ACContext.getInstance();

    protected static JSONObject postObject(String relativePath, ACObject acObject) throws IOException {
        return getRequestProcessor().processPost(acObject, relativePath);

    }

    public long getId() {
        return _id;
    }

    public String getUrl() {
        return _url;
    }

    public Date getCreatedOn() {
        return _createdOn;
    }

    public long getCreatedById() {
        return _createdById;
    }

    public Date getUpdatedOn() {
        return _updatedOn;
    }

    public long getUpdatedById() {
        return _updatedById;
    }

    public abstract String toJSONString();

    public void setId(long _id) {
        this._id = _id;
    }

    public static RequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    public static void setRequestProcessor(RequestProcessor requestProcessor) {
        ACObject.requestProcessor = requestProcessor;
    }
}
