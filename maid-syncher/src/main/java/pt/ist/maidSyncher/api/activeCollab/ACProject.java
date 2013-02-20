package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ACProject extends ACObject {

    // attributes
    private String _name;
    private String _overview;
    private int _categoryId;
    private int _companyId;
    private int _leaderId;
    private String _status;
    private float _budget;
    private int _labelId;

    public ACProject()
    {
        super();
        _categoryId = -1;
        _companyId = -1;
        _leaderId = -1;
        _labelId = -1;
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
        getTasks();

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

    public List<ACMilestone> getMilestones() throws IOException {
        List<ACMilestone> milestones = new ArrayList<ACMilestone>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet(_url + "/milestones");
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
        JsonRest.setInt(postData, "project[category_id]", _categoryId);
        JsonRest.setInt(postData, "project[company_id]", _companyId);
        JsonRest.setInt(postData, "project[leader_id]", _leaderId);
        JsonRest.setString(postData, "project[status]", _status);
        JsonRest.setFloat(postData, "project[budget]", _budget);
        JsonRest.setInt(postData, "project[label_id]", _labelId);
        return postData.toString();
    }

    public List<ACLoggedTime> getLoggedTimes() throws IOException {
        List<ACLoggedTime> loggedTimes = new ArrayList<ACLoggedTime>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet(_url + "/tracking&dont_limit_result=1");
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

    public List<ACTask> getTasks() throws IOException
    {
        List<ACTask> tasks = new ArrayList<ACTask>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet(_url + "/tasks");
        if(jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                tasks.add(new ACTask(jsonObject));
            }
        }
        return tasks;
    }

}
