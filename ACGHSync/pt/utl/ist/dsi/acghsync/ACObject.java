package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import java.util.Date;

import org.json.simple.JSONObject;

public class ACObject {

	protected int _id;
	protected String _url;
	protected Date _createdOn;
	protected int _createdById;
	protected Date _updatedOn;
	protected int _updatedById;
	
	public ACObject()
	{
		_id = -1;
		_createdById = -1;
		_updatedById = -1;
	}
	
	public ACObject(JSONObject jsonObj) throws IOException
	{
		_id = JsonRest.getInt(jsonObj, "id");
		_url = JsonRest.getString(jsonObj, "permalink");
		_url = _url.replaceFirst("public/index","api");
		System.out.println("URL:" +  _url);
		_createdOn = JsonRest.getDate(jsonObj, "created_on");
		_createdById = JsonRest.getInt(jsonObj, "created_by_id");
		_updatedOn = JsonRest.getDate(jsonObj, "updated_on");
		_updatedById = JsonRest.getInt(jsonObj, "updated_by_id");
	}
	
	public int getId() {
		return _id;
	}
	
	public String getUrl() {
		return _url;
	}
	
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	public int getCreatedById() {
		return _createdById;
	}
	
	public Date getUpdatedOn() {
		return _updatedOn;
	}
	
	public int getUpdatedById() {
		return _updatedById;
	}
}
