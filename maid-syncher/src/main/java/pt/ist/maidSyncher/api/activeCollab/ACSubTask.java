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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

public class ACSubTask extends ACObject {

    // attributes
    private String name;
    private int _assigneeId;
    private int _priority;
    private int _labelId;
    private Date _dueOn;
    private long parentId;
    private String parentClass;

    private boolean archived;

    private boolean complete;

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

    public ACSubTask update(String url) throws IOException {
        checkArgument(StringUtils.isBlank(url) == false);
        //let us construct the URL and send an edit
        return new ACSubTask(ACSubTask.postObject(url, toJSONString()));
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
        setParentClass(JsonRest.getString(jsonObj, "parent_class"));
        setParentId(JsonRest.getInt(jsonObj, "parent_id"));
        setArchived(JsonRest.getBooleanFromInt(jsonObj, "is_archived"));
        setComplete(JsonRest.getBooleanFromInt(jsonObj, "is_completed"));
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

    public String getParentClass() {
        return parentClass;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
