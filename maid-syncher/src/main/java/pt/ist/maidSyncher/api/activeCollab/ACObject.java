package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;
import java.util.Date;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ACObject {

    public static final Logger LOGGER = LoggerFactory.getLogger(ACObject.class);

    protected long _id;
    protected String _url;
    protected Date _createdOn;
    protected long _createdById;
    protected Date _updatedOn;
    protected long _updatedById;

    public ACObject()
    {
        _id = -1;
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
        _id = JsonRest.getInt(jsonObj, "id");
        _url = JsonRest.getString(jsonObj, "permalink");
        _url = _url.replaceFirst("public/index","api");

        LOGGER.trace("URL:" + _url);

        _createdOn = JsonRest.getDate(jsonObj, "created_on");
        _createdById = JsonRest.getInt(jsonObj, "created_by_id");
        _updatedOn = JsonRest.getDate(jsonObj, "updated_on");
        _updatedById = JsonRest.getInt(jsonObj, "updated_by_id");

    }

    protected final void updateFromPermalink() throws IOException {
        //let's use the permalink to get all of the information
        JSONObject jsonObject = (JSONObject) ACContext.processGet(_url);
        init(jsonObject);

    }

    protected static JSONObject createObject(String relativePath, String content) throws IOException {
        return ACContext.processPost(content, relativePath);

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
}
