package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONObject;

public class ACSubTask extends ACObject {

    // attributes
    private String name;
    private int _assigneeId;
    private int _priority;
    private int _labelId;
    private Date _dueOn;

    public ACSubTask()
    {
        super();
        _assigneeId = -1;
        _priority = -1;
        _labelId = -1;
    }

    public ACSubTask(JSONObject jsonObj) throws IOException
    {
        super(jsonObj);
        init(jsonObj);

    }

    @Override
    protected void init(JSONObject jsonObj) {
        setName(JsonRest.getString(jsonObj, "name"));
        _assigneeId = JsonRest.getInt(jsonObj, "assignee_id");
        _priority = JsonRest.getInt(jsonObj, "priority");
        _labelId = JsonRest.getInt(jsonObj, "label_id");
        _dueOn = JsonRest.getDate(jsonObj, "due_on");
        if(_dueOn != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(_dueOn);
            cal.set(Calendar.HOUR_OF_DAY,23);
            cal.set(Calendar.MINUTE,59);
            cal.set(Calendar.SECOND,59);
            _dueOn = cal.getTime();
        }
    }

    public int getAssigneeId() {
        return _assigneeId;
    }

    public void setAssigneeId(int assigneeId) {
        _assigneeId = assigneeId;
    }

    public int getPriority() {
        return _priority;
    }

    public void setPriority(int priority) {
        _priority = priority;
    }

    public int getLabelId() {
        return _labelId;
    }

    public void setLabelId(int labelId) {
        _labelId = labelId;
    }

    public Date getDueOn() {
        return _dueOn;
    }

    public void setDueOn(Date dueOn) {
        _dueOn = dueOn;
    }

    @Override
    public String toJSONString()
    {
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "subtask[body]", getName());
//		JsonRest.setInt(postData, "subtask[assignee_id]", _assigneeId);
        JsonRest.setInt(postData, "subtask[priority]", _priority);
//		JsonRest.setInt(postData, "subtask[label_id]", _labelId);
        JsonRest.setDate(postData, "subtask[due_on]", _dueOn);
        return postData.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
