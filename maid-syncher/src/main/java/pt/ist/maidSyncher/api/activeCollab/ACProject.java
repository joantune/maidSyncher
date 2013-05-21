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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ACProject extends ACObject {

    public static final String CLASS_VALUE = "Project";

    // attributes
    private String _name;
    private String _overview;
    private int _categoryId;
    private int _companyId;
    private int _leaderId;
    private String _status;
    private float _budget;
    private int _labelId;
    private boolean archived;

    public ACProject()
    {
        super();
        _categoryId = -1;
        _companyId = -1;
        _leaderId = -1;
        _labelId = -1;
        _budget = -1;
        this.setArchived(false);
    }

    public ACProject(JSONObject jsonObj) throws IOException
    {
        super(jsonObj);

    }

    @Override
    protected void init(JSONObject jsonObj) throws IOException {
        setName(JsonRest.getString(jsonObj, "name"));
        setOverview(JsonRest.getString(jsonObj, "overview"));
        setCategoryId(JsonRest.getInt(jsonObj, "category_id"));
        setCompanyId(JsonRest.getInt(jsonObj, "company_id"));
        setLeaderId(JsonRest.getInt(jsonObj, "leader_id"));
        setStatus(JsonRest.getString(jsonObj, "status_verbose"));
        setBudget(JsonRest.getFloat(jsonObj, "budget"));
        setLabelId(JsonRest.getInt(jsonObj, "label_id"));
        setArchived(JsonRest.getBooleanFromInt(jsonObj, "is_archived"));
//        getTasks();

    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

    public void setOverview(String overview)
    {
        _overview = overview;
    }

    public String getOverview()
    {
        return _overview;
    }

    public int getCategoryId() {
        return _categoryId;
    }

    public void setCategoryId(int categoryId) {
        _categoryId = categoryId;
    }

    public int getCompanyId() {
        return _companyId;
    }

    public void setCompanyId(int companyId) {
        _companyId = companyId;
    }

    public int getLeaderId() {
        return _leaderId;
    }

    public void setLeaderId(int leaderId) {
        _leaderId = leaderId;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        _status = status;
    }

    public float getBudget() {
        return _budget;
    }

    public void setBudget(float budget) {
        _budget = budget;
    }

    public int getLabelId() {
        return _labelId;
    }

    public void setLabelId(int labelId) {
        _labelId = labelId;
    }

    public static ACProject create(ACProject preliminarProject) throws IOException {
        String path = getRequestProcessor().getBasicUrlForPath("projects/add");
        return new ACProject(postObject(path, preliminarProject));

    }

    public List<ACMilestone> getMilestones() throws IOException {
        List<ACMilestone> milestones = new ArrayList<ACMilestone>();
        JSONArray jsonArr = (JSONArray) getRequestProcessor().processGet(getUrl() + "/milestones");
        if (jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                milestones.add(new ACMilestone(jsonObject));
            }
        }
        return milestones;

    }

    @Override
    public String toJSONString()
    {
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "project[name]", _name);
        JsonRest.setString(postData, "project[overview]", _overview);
        JsonRest.setIntIfInitialized(postData, "project[category_id]", _categoryId);
        JsonRest.setIntIfInitialized(postData, "project[company_id]", _companyId);
        JsonRest.setIntIfInitialized(postData, "project[leader_id]", _leaderId);
        JsonRest.setString(postData, "project[status]", _status);
        JsonRest.setFloatIfInitialized(postData, "project[budget]", _budget);
        JsonRest.setIntIfInitialized(postData, "project[label_id]", _labelId);
        return postData.toString();
    }

//    public String toJSONString(String... fields) {
//        StringBuilder postData = new StringBuilder();
//        for (String field : fields) {
//            switch (field) {
//            case "name":
//                break;
//            case "overview":
//                break;
//            case "category_id":
//                break;
//            case "company_id":
//                break;
//            case "leader_id":
//            }
//        }
//        JsonRest.setString(postData, "project[name]", _name);
//        JsonRest.setString(postData, "project[overview]", _overview);
//        JsonRest.setInt(postData, "project[category_id]", _categoryId);
//        JsonRest.setInt(postData, "project[company_id]", _companyId);
//        JsonRest.setInt(postData, "project[leader_id]", _leaderId);
//        JsonRest.setString(postData, "project[status]", _status);
//        JsonRest.setFloat(postData, "project[budget]", _budget);
//        JsonRest.setInt(postData, "project[label_id]", _labelId);
//        return postData.toString();
//    }

    public Set<ACCategory> getTaskCategories() throws IOException {
        Set<ACCategory> taskCategories = new HashSet<ACCategory>();
        JSONArray jsonArr = (JSONArray) getRequestProcessor().processGet(getUrl() + "/tasks/categories");
        if (jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                String classValue = JsonRest.getString(jsonObject, "class");
                taskCategories.add(new ACCategory(jsonObject));
            }
        }
        return taskCategories;
    }

    public List<ACLoggedTime> getLoggedTimes() throws IOException {
        List<ACLoggedTime> loggedTimes = new ArrayList<ACLoggedTime>();
        JSONArray jsonArr = (JSONArray) getRequestProcessor().processGet(getUrl() + "/tracking&dont_limit_result=1");
        if (jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                String classValue = JsonRest.getString(jsonObject, "class");
                if (classValue.equalsIgnoreCase(ACLoggedTime.CLASS_VALUE))
                    loggedTimes.add(new ACLoggedTime(jsonObject));
            }
        }
        return loggedTimes;

    }

    public ACProject update() throws IOException {
        //let us construct the URL and send an edit
        String path = getRequestProcessor().getBasicUrlForPath("projects/" + this.getId() + "/edit");
        return new ACProject(postObject(path, this));

    }

    public List<ACTask> getTasks() throws IOException
    {
        List<ACTask> tasks = new ArrayList<ACTask>();
        JSONArray jsonArr = (JSONArray) getRequestProcessor().processGet(getUrl() + "/tasks");
        if(jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                tasks.add(new ACTask(jsonObject));
            }
        }
        return tasks;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

}
