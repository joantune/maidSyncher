/**
 * 
 */
package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;

import org.json.simple.JSONObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 13 de Fev de 2013
 *
 * 
 */
public class ACLoggedTime extends ACObject {

    protected final static String CLASS_VALUE = "TimeRecord";


    private int userId;
    private String name;
    private String parentClass;
    private int parentId;
    private Boolean isArchived;
    private Boolean isTrashed;
    private String summary;
    private int jobTypeId;
    private float value;


    //private ACUser user - we can actually materialize the user TODO ?

    public ACLoggedTime(JSONObject jsonObject) throws IOException {
        super(jsonObject);
    }

    @Override
    protected void init(JSONObject jsonObj) {
        JSONObject user = JsonRest.getJSONObject(jsonObj, "user");
        setUserId(JsonRest.getInt(user, "id"));
        setName(JsonRest.getString(jsonObj, "name"));
        setParentClass(JsonRest.getString(jsonObj, "parent_class"));
        setParentId(JsonRest.getInt(jsonObj, "parent_id"));
        setIsArchived(JsonRest.getBooleanFromInt(jsonObj, "is_archived"));
        setIsTrashed(JsonRest.getBooleanFromInt(jsonObj, "is_trashed"));
        setSummary(JsonRest.getString(jsonObj, "summary"));
        setJobTypeId(JsonRest.getInt(jsonObj, "job_type_id"));
        setValue(JsonRest.getFloat(jsonObj, "value"));

    }


    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.ACObject#toJSONString()
     */
    @Override
    public String toJSONString() {
        // TODO Auto-generated method stub
        return null;
    }



    public void setName(String name) {
        this.name = name;
    }

    public String getParentClass() {
        return parentClass;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getJobTypeId() {
        return jobTypeId;
    }

    public void setJobTypeId(int jobTypeId) {
        this.jobTypeId = jobTypeId;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Boolean getIsTrashed() {
        return isTrashed;
    }

    public void setIsTrashed(Boolean isTrashed) {
        this.isTrashed = isTrashed;
    }

    public String getName() {
        return name;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

}
