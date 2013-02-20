package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class GHIssue {

	private JSONObject _jsonObj;
	private boolean _parsed;
	
	private int _id;
	private int _number;
	private String _title;
	private String _body;
	private String _state;
	
	public GHIssue()
	{
		_parsed = true;
	}
	
	public GHIssue(JSONObject jsonObj)
	{
		_jsonObj = jsonObj;
		_parsed = false;
	}
	
	private void parse() throws IOException {
	    Number number;
        _id = ((java.lang.Number) _jsonObj.get("id")).intValue(); 
        _number = ((java.lang.Number) _jsonObj.get("number")).intValue(); 
	    _title = (String) _jsonObj.get("title");
        _body = (String) _jsonObj.get("body");
        _state = (String) _jsonObj.get("state");
	    _parsed = true;
	}
	
	public int getId() throws IOException
	{
		if(!_parsed)
			parse();
		return _id;
	}

	public int getNumber() throws IOException
	{
		if(!_parsed)
			parse();
		return _number;
	}

	public String getTitle() throws IOException
	{
		if(!_parsed)
			parse();
		return _title;
	}

	public void setTitle(String title) throws IOException
	{
		_title = title;
	}

	public String getBody() throws IOException
	{
		if(!_parsed)
			parse();
		return _body;
	}

	public void setBody(String body) throws IOException
	{
		_body = body;
	}
	
	public String getState() throws IOException
	{
		if(!_parsed)
			parse();
		return _state;
	}
	
	public void setState(String state) throws IOException
	{
		_state = state;
	}
	
	public List getComments() throws IOException
	{
		List<GHComment> comments = new ArrayList<GHComment>();
		JSONArray jsonArr = (JSONArray) GHContext.processGet("ljbmgs/test/issues/" + _number + "/comments");
	    System.out.println("  #Comments: " + jsonArr.size());
        for(int i = 0; i < jsonArr.size(); i++)
        	comments.add(new GHComment((JSONObject)jsonArr.get(i)));
        return comments;
	}
}