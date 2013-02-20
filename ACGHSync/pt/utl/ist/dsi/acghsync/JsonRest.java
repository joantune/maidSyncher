package pt.utl.ist.dsi.acghsync;

import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonRest {

	
	public static int getInt(JSONObject jsonObj, String key)
	{
		int value = 0;
		Object obj = jsonObj.get(key);
		if(obj != null)
			value = ((java.lang.Number) obj).intValue();
		return value;
	}
	
	public static void setInt(StringBuilder postData, String key, int value)
	{
		if(postData.length() > 0)
			postData.append("&");
		postData.append(key + "=" + value);	
	}
	
	public static float getFloat(JSONObject jsonObj, String key)
	{
		float value = 0.F;
		Object obj = jsonObj.get(key);
		if(obj != null)
			value = ((java.lang.Number) obj).floatValue();
		return value;
	}
	
	public static void setFloat(StringBuilder postData, String key, float value)
	{
		if(postData.length() > 0)
			postData.append("&");
		postData.append(key + "=" + (float) value);	
	}
	
	
	public static String getString(JSONObject jsonObj, String key)
	{
		String value;
		value = (String) jsonObj.get(key);
		return value;
	}
	
	public static void setString(StringBuilder postData, String key, String value)
	{
		if(value != null) {
			if(postData.length() > 0)
				postData.append("&");
			try {
			postData.append(key + "=" + URLEncoder.encode(value, "utf-8"));
			} catch (UnsupportedEncodingException uee) {
				
			}
		}
	}
	
	public static Date getDate(JSONObject jsonObj, String key)
	{
		Date value = null;
		if(jsonObj != null) {
			JSONObject jsonObj2 = (JSONObject) jsonObj.get(key);
			if(jsonObj2 != null) {
				Object obj = jsonObj2.get("timestamp");
				if(obj != null) {
					long number;
					number = (((java.lang.Number) obj).longValue());
					value = new Date(1000*number);
				}
			}
		}
		return value;
	}
	
	public static void setDate(StringBuilder postData, String key, Date value)
	{
		if(value != null) {
			if(postData.length() > 0)
				postData.append("&");
			postData.append(key + "=");
			Calendar cal = Calendar.getInstance();
			cal.setTime(value);
			postData.append(cal.get(Calendar.YEAR) + "-");
			if(cal.get(Calendar.MONTH) < 10)
				postData.append("0");
			postData.append((1+cal.get(Calendar.MONTH)) + "-");
			if(cal.get(Calendar.DAY_OF_MONTH) < 10)
				postData.append("0");
			postData.append(cal.get(Calendar.DAY_OF_MONTH));
		}	
	}
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1)
			sb.append((char) cp);
		return sb.toString();
	}
	
	public static Object processGet(String urlStr) throws IOException
	{
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true); 
		conn.setDoInput(true);
		conn.setInstanceFollowRedirects(false); 
		conn.setRequestMethod("GET"); 
		conn.setRequestProperty("Content-Type", "text/plain"); 
		conn.setRequestProperty("charset", "utf-8");
		conn.connect();
		InputStream is = conn.getInputStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			System.out.println(jsonText);
			return JSONValue.parse(jsonText);
		} finally {
			is.close();
			conn.disconnect();
		}	  
	}
	
	public static Object processPost(String urlStr, String content) throws IOException
	{
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setInstanceFollowRedirects(false); 
		conn.setRequestMethod("POST"); 
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", "" + Integer.toString(content.getBytes("utf-8").length));
		conn.setUseCaches (false);	  
		conn.connect();
		
		//Send request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes (content);
		wr.flush ();
		wr.close ();

		System.out.println("ACURL [" + url + "]");
		InputStream is = conn.getInputStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
			String jsonText = readAll(rd);
			return jsonText;
		} finally {
			is.close();
			conn.disconnect();
		}	  
	}			

	/*
	public static Object processPost(String urlStr, Map<String,String> para) throws IOException 
	{
		StringBuilder urlPara = new StringBuilder();
		boolean notFirst = false;
		for(Iterator<Map.Entry<String,String>> it = para.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String,String> e = (Map.Entry<String,String>) it.next();
			if(notFirst)
				urlPara.append("&");
			else
				notFirst = true;
			urlPara.append(URLEncoder.encode((String)e.getKey(), "utf-8"));
			urlPara.append("=");
			urlPara.append(URLEncoder.encode((String)e.getValue(), "utf-8"));
		}
		return processPost(urlStr, urlPara.toString());
	}
	*/
}
