package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class GHComment {

	private JSONObject _jsonObj;
	private boolean _parsed;
	
	private int _id;
	private String _body;
	private String _user;
	
	public GHComment()
	{
		_parsed = true;
	}
	
	public GHComment(JSONObject jsonObj)
	{
		_jsonObj = jsonObj;
		_parsed = false;
	}
	
	private void parse() throws IOException {
        _id = ((java.lang.Number) _jsonObj.get("id")).intValue(); 
        _body = (String) _jsonObj.get("body");
        _user = (String)((JSONObject) _jsonObj.get("user")).get("login");
	    _parsed = true;
	}
	
	public int getId() throws IOException
	{
		if(!_parsed)
			parse();
		return _id;
	}

	public String getBody() throws IOException
	{
		if(!_parsed)
			parse();
		return _body;
	}

	public void setBody(String body)
	{
		_body = body;
	}
	
	public String getUser() throws IOException
	{
		if(!_parsed)
			parse();
		return _user;
	}
	
	public void setUser(String user)
	{
		_user = user;
	}
}
