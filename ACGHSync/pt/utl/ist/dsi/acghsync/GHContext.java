package pt.utl.ist.dsi.acghsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.simple.parser.ParseException;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import pt.utl.ist.dsi.acghsync.*;

public class GHContext {

	private static String _server = "api.github.com";
	private static String _username;
	private static String _password;
 
	public static void setServer(String server) 
	{
		_server = server;
	}
  
	public static void setUsername(String username) 
	{
		_username = username;
	}
  
	public static void setPassword(String password)
	{
		_password = password;
	}
  
	private static String buildUrl(String path) {
		String url = "https://" + _username + ":" + _password + "@" + _server + "/repos";
		if(path.length() > 0)
			url += "/" + path;
		return url;
	}

	public static Object processGet(String path) throws IOException
	{
		return JsonRest.processGet(buildUrl(path));
	}
}
