package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import org.json.simple.JSONObject;

public class ACComment extends ACObject {

	// attributes
	private String _body;
  
    public ACComment()
    {
    	super();
    }
    
	public ACComment(JSONObject jsonObj) throws IOException
	{
		super(jsonObj);
	    _body = JsonRest.getString(jsonObj, "body");
	}
	
	public String getBody() {
		return _body;
	}
	
	public void setBody(String body) {
		_body = body;
	}
	
	public String toString()
	{
		StringBuilder postData = new StringBuilder();
		JsonRest.setString(postData, "task[body]", _body);
		return postData.toString();
	}
}
