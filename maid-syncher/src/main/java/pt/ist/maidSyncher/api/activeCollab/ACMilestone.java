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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
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
    private long projectId;

    public ACMilestone()
    {
        super();
    }

    public ACMilestone(JSONObject jsonObj) throws IOException
    {
        super(jsonObj);

    }

    @Override
    protected void init(JSONObject jsonObj) throws IOException {
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
        this.setProjectId(JsonRest.getInt(jsonObj, "project_id"));

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

    static public ACMilestone copyTo(long milestoneId, long currentProjectId, long newProjectId) throws IOException {
        String path =
                getRequestProcessor().getBasicUrlForPath("projects/" + currentProjectId + "/milestones/" + milestoneId
                        + "/copy-to-project");
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "copy_to_project_id", String.valueOf(newProjectId));
        return new ACMilestone(getRequestProcessor().processPost(path, postData.toString()));

    }

    static public ACMilestone moveTo(long milestoneId, long currentProjectId, long newProjectId) throws IOException {
        String path =
                getRequestProcessor().getBasicUrlForPath("projects/" + currentProjectId + "/milestones/" + milestoneId
                        + "/move-to-project");
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "move_to_project_id", String.valueOf(newProjectId));
        return new ACMilestone(getRequestProcessor().processPost(path, postData.toString()));

    }

    public static ACMilestone create(ACMilestone preliminarObject) throws IOException {
        checkNotNull(preliminarObject);
        checkArgument(preliminarObject.getProjectId() > 0);
        String path = getRequestProcessor().getBasicUrlForPath("projects/" + preliminarObject.getProjectId() + "/milestones/add");
        return new ACMilestone(postObject(path, preliminarObject));
    }

    /**
     * 
     * @param url the base url of the object to update
     * @return
     * @throws IOException
     */
    public ACMilestone update(String url) throws IOException {
        checkArgument(StringUtils.isBlank(url) == false);
        //let us construct the URL and send an edit
        url += "/edit";
        return new ACMilestone(ACMilestone.postObject(url, this));
    }

    @Override
    public String toJSONString()
    {
        StringBuilder postData = new StringBuilder();
        JsonRest.setString(postData, "milestone[name]", _name);
        JsonRest.setString(postData, "milestone[body]", _body);
        JsonRest.setInt(postData, "milestone[priority]", _priority);
        JsonRest.setInt(postData, "milestone[assignee_id]", _assigneeId);
        JsonRest.setDate(postData, "milestone[start_on]", _startOn);
        JsonRest.setDate(postData, "milestone[due_on]", _dueOn);
        return postData.toString();
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

}
