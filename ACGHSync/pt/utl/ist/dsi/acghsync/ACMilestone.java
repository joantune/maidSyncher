package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import org.json.simple.JSONObject;

public class ACMilestone extends ACObject {

	// attributes
	private String _name;
	private String _body;
    private int _priority;
    private int _assigneeId;
//	private int* _otherAssigneesId;
    private Date _startOn;
    private Date _dueOn;
  
    public ACMilestone()
    {
    	super();
    }
    
	public ACMilestone(JSONObject jsonObj) throws IOException
	{
		super(jsonObj);
	    _name = JsonRest.getString(jsonObj, "name");
	    _body = JsonRest.getString(jsonObj, "body");
	    _priority = JsonRest.getInt(jsonObj, "priority");
	    _assigneeId = JsonRest.getInt(jsonObj, "assignee_id");
	    _startOn = JsonRest.getDate(jsonObj, "start_on");
	    if(_startOn != null) {
	    	Calendar cal = Calendar.getInstance();
	    	cal.setTime(_startOn);
	    	cal.set(Calendar.HOUR_OF_DAY,23);
	    	cal.set(Calendar.MINUTE,59);
	    	cal.set(Calendar.SECOND,59);
	    	_startOn = cal.getTime();
	    }
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

	public Date getStartOn() {
		return _startOn;
	}
	
	public void setStartOn(Date startOn) {
		_startOn = startOn;
	}
	
	public Date getDueOn() {
		return _dueOn;
	}
	
	public void setDueOn(Date dueOn) {
		_dueOn = dueOn;
	}
	
	public String toString()
	{
		StringBuilder postData = new StringBuilder();
		JsonRest.setString(postData, "task[name]", _name);
		JsonRest.setString(postData, "task[body]", _body);
		JsonRest.setInt(postData, "task[priority]", _priority);
		JsonRest.setInt(postData, "task[assignee_id]", _assigneeId);
		JsonRest.setDate(postData, "task[start_on]", _startOn);
		JsonRest.setDate(postData, "task[due_on]", _dueOn);
		return postData.toString();
	}
}
