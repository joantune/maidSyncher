package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class GHRepo {

	// parsing related
	private boolean _parsed;
	
	private String _fullName;
	private int _id;
    private String _name;	
	
	public GHRepo(String fullName) throws IOException
	{
		_fullName = fullName;
		_parsed = false;
	}

	private void parse() throws IOException
	{
		JSONObject jsonObj = (JSONObject) GHContext.processGet("ljbmgs/test");
       	_id = ((java.lang.Number) jsonObj.get("id")).intValue(); 
    	_name = (String) jsonObj.get("name");
    	_parsed = true;
	}
	
	public int getId() throws IOException
	{
		if(!_parsed)
			parse();
		return _id;
	}
	
	public String getName() throws IOException
	{
		if(!_parsed)
			parse();
		return _name;
	}
	
	public List<GHIssue> getIssues() throws IOException
	{
		List<GHIssue> issues = new ArrayList<GHIssue>();
		JSONArray jsonArr = (JSONArray) GHContext.processGet("ljbmgs/test/issues");
	    System.out.println("#Issues: " + jsonArr.size());
        for(int i = 0; i < jsonArr.size(); i++)
        	issues.add(new GHIssue((JSONObject)jsonArr.get(i)));
        return issues;
	}
}