package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ACTask extends ACObject {

    // attributes
    private String _name;
    private String _body;
    private int _visibility;
    private int _categoryId;
    private int _labelId;
    private int _milestoneId;
    private int _priority;
    private int _assigneeId;
//	private int* _otherAssigneesId;
    private Date _dueOn;

    private Boolean complete;

    public ACTask()
    {
        super();
    }

    public ACTask(JSONObject jsonObj) throws IOException
    {
        super(jsonObj);

    }

    @Override
    protected void init(JSONObject jsonObj) throws IOException {
        _name = JsonRest.getString(jsonObj, "name");
        _body = JsonRest.getString(jsonObj, "body");
        _visibility = JsonRest.getInt(jsonObj, "visibility");
        _categoryId = JsonRest.getInt(jsonObj, "category_id");
        _labelId = JsonRest.getInt(jsonObj, "label_id");
        _milestoneId = JsonRest.getInt(jsonObj, "milestone_id");
        _priority = JsonRest.getInt(jsonObj, "priority");
        _assigneeId = JsonRest.getInt(jsonObj, "assignee_id");
        _dueOn = JsonRest.getDate(jsonObj, "due_on");
        if(_dueOn != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(_dueOn);
            cal.set(Calendar.HOUR_OF_DAY,23);
            cal.set(Calendar.MINUTE,59);
            cal.set(Calendar.SECOND,59);
            _dueOn = cal.getTime();
        }
        getSubTasks();

        setComplete(JsonRest.getBooleanFromInt(jsonObj, "is_completed"));

    }

    @Override
    public String toJSONString()
    {
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "task[name]", _name);
        JsonRest.setString(postData, "task[body]", _body);
        JsonRest.setInt(postData, "task[visibility]", _visibility);
        JsonRest.setInt(postData, "task[category_id]", _categoryId);
        JsonRest.setInt(postData, "task[label_id]", _labelId);
        JsonRest.setInt(postData, "task[milestone_id]", _milestoneId);
        JsonRest.setInt(postData, "task[priority]", _priority);
        JsonRest.setInt(postData, "task[assignee_id]", _assigneeId);
        JsonRest.setDate(postData, "task[due_on]", _dueOn);
        return postData.toString();
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getBody() {
        return _body;
    }

    public void setBody(String body) {
        _body = body;
    }

    public int getVisibility() {
        return _visibility;
    }

    public void setVisibility(int visibility) {
        _visibility = visibility;
    }

    public int getCategoryId() {
        return _categoryId;
    }

    public void setCategoryId(int categoryId) {
        _categoryId = categoryId;
    }

    public int getLabelId() {
        return _labelId;
    }

    public void setLabelId(int labelId) {
        _labelId = labelId;
    }

    public int getMilestoneId() {
        return _milestoneId;
    }

    public void setMilestoneId(int milestoneId) {
        _milestoneId = milestoneId;
    }

    public int getPriority() {
        return _priority;
    }

    public void setPriority(int priority) {
        _priority = priority;
    }

    public int getAssigneeId() {
        return _assigneeId;
    }

    public void setAssigneeId(int assigneeId) {
        _assigneeId = assigneeId;
    }

    public Date getDueOn() {
        return _dueOn;
    }

    public void setDueOn(Date dueOn) {
        _dueOn = dueOn;
    }

    public List<ACSubTask> getSubTasks() throws IOException
    {
        List<ACSubTask> subtasks = new ArrayList<ACSubTask>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet(_url + "/subtasks");
        if(jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                subtasks.add(new ACSubTask(jsonObject));
            }
        }
        return subtasks;
    }

    public List<ACComment> getComments() throws IOException {
        List<ACComment> comments = new ArrayList<ACComment>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet(_url + "/comments");
        //this URL has the

        if (jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObject = (JSONObject) object;
                comments.add(new ACComment(jsonObject));
            }
        }
        return comments;

    }

    public ACCategory getCategory() throws IOException {
        if (this._categoryId == -1 || this._categoryId == 0)
            return null;
        String decodedUrl = URLDecoder.decode(_url, "UTF-8");
        int lastIndexOfSlash = StringUtils.lastIndexOf(decodedUrl, "/");
        String urlToUse = StringUtils.substring(decodedUrl, 0, lastIndexOfSlash);
        return new ACCategory((JSONObject) ACContext.processGet(urlToUse + "/categories/" + _categoryId));

    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }
}
