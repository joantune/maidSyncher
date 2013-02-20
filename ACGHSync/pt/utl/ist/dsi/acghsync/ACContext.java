package pt.utl.ist.dsi.acghsync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


public class ACContext {

  private static String _server;
  private static String _token;
 
  public static void setServer(String server) 
  {
	  _server = server;
  }
  
  public static void setToken(String token)
  {
	  _token = token;
  }
  
  private static String buildUrl(String path) {
	  String url = path + "&auth_api_token=" + _token + "&format=json";
	  return url;
  }	

  public static Object processGet(String path) throws IOException
  {
	  return JsonRest.processGet(buildUrl(path));
  }
  
  public static List<ACProject> getProjects() throws IOException
  {
  		List<ACProject> projects = new ArrayList<ACProject>();
	  	JSONArray jsonArr = (JSONArray) ACContext.processGet("https://" + _server + "/ac/api.php?path_info=projects"); 
	  	if(jsonArr != null) {
	  		for(int i = 0; i < jsonArr.size(); i++)
	  			projects.add(new ACProject((JSONObject)jsonArr.get(i)));
	  	}
	  	return projects;	  
  }
  
}
