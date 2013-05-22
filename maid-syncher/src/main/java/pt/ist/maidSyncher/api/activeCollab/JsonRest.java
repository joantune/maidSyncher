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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonRest {

    public static final Logger LOGGER = LoggerFactory.getLogger(JsonRest.class);

    public static JSONObject getJSONObject(JSONObject jsonObject, String key) {
        return (JSONObject) jsonObject.get(key);
    }

    public static JSONArray getJSONArray(JSONObject jsonObject, String key) {
        return (JSONArray) jsonObject.get(key);
    }

    public static int getInt(JSONObject jsonObj, String key) {
        int value = -1;
        Object obj = jsonObj.get(key);
        if (obj != null)
            value = ((java.lang.Number) obj).intValue();
        return value;
    }

    public static void setInt(StringBuilder postData, String key, int value) {
        if (postData.length() > 0)
            postData.append("&");
        postData.append(key + "=" + value);
    }

    public static void setIntFromBoolean(StringBuilder postData, String key, boolean value) {
        if (postData.length() > 0)
            postData.append("&");
        postData.append(key + "=" + (value ? "1" : "0"));
    }

    public static float getFloat(JSONObject jsonObj, String key) {
        float value = 0.F;
        Object obj = jsonObj.get(key);
        if (obj != null)
            value = ((java.lang.Number) obj).floatValue();
        return value;
    }

    public static void setFloat(StringBuilder postData, String key, float value) {
        if (postData.length() > 0)
            postData.append("&");
        postData.append(key + "=" + value);
    }

    public static String getString(JSONObject jsonObj, String key) {
        String value;
        value = (String) jsonObj.get(key);
        return value;
    }

    public static Boolean getBooleanFromInt(JSONObject jsonObj, String key) {
        int booleanInt = getInt(jsonObj, key);
        return booleanInt == -1 ? null : booleanInt > 0;
    }

    public static Boolean getBooleanFromString(JSONObject jsonObj, String key) {
        return (Boolean) jsonObj.get(key);
    }

    public static void setString(StringBuilder postData, String key, String value) {
        if (value != null) {
            if (postData.length() > 0)
                postData.append("&");
            try {
                postData.append(key + "=" + URLEncoder.encode(value, "utf-8"));
            } catch (UnsupportedEncodingException uee) {

            }
        }
    }

    public static Date getDate(JSONObject jsonObj, String key) {
        Date value = null;
        if (jsonObj != null) {
            JSONObject jsonObj2 = (JSONObject) jsonObj.get(key);
            if (jsonObj2 != null) {
                Object obj = jsonObj2.get("timestamp");
                if (obj != null) {
                    long number;
                    number = (((java.lang.Number) obj).longValue());
                    value = new Date(1000 * number);
                }
            }
        }
        return value;
    }

    public static void setDate(StringBuilder postData, String key, Date value) {
        if (value != null) {
            if (postData.length() > 0)
                postData.append("&");
            postData.append(key + "=");
            Calendar cal = Calendar.getInstance();
            cal.setTime(value);
            postData.append(cal.get(Calendar.YEAR) + "-");
            if (cal.get(Calendar.MONTH) < 10)
                postData.append("0");
            postData.append((1 + cal.get(Calendar.MONTH)) + "-");
            if (cal.get(Calendar.DAY_OF_MONTH) < 10)
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

    public static Object processGet(String urlStr) throws IOException {
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
            LOGGER.trace(jsonText);
            return JSONValue.parse(jsonText);
        } finally {
            is.close();
            conn.disconnect();
        }
    }

    public static Object processPost(String content, String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", "" + Integer.toString(content.getBytes("utf-8").length));
        conn.setUseCaches(false);
        conn.connect();

        //Send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(content);
        wr.flush();
        wr.close();

        LOGGER.trace("ACURL [" + url + "]");

        InputStream is = null;
        try {
            is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String jsonText = readAll(rd);
            return JSONValue.parse(jsonText);
        } catch (IOException ex) {
            int responseCode = conn.getResponseCode();
            if ((responseCode / 100) != 2) {
                InputStream errorStream = conn.getErrorStream();
                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(errorStream, Charset.forName("utf-8")));
                    Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                    JsonElement jsonErrorBody = new JsonParser().parse(rd);
                    LOGGER.error("Error: \n" + gsonBuilder.toJson(jsonErrorBody) + "\nOriginal request body:\n" + content
                            + "\nURL: " + url);
                } finally {
                    errorStream.close();
                    conn.disconnect();
                }

            }
            throw ex;
        } finally {
            if (is != null)
                is.close();
            conn.disconnect();
        }
    }

    public static void setIntIfInitialized(StringBuilder postData, String key, int value) {
        if (value != -1)
            setInt(postData, key, value);
        else
            return;
    }

    public static void setFloatIfInitialized(StringBuilder postData, String key, float value) {
        if (value != -1) {
            setFloat(postData, key, value);
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
