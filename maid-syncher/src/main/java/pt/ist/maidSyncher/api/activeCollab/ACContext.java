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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ACContext {

    private static String server;
    private static String token;

    private static final ACContext instance = new ACContext();

    private ACContext() {
    }

    public static ACContext getInstance() {
        return instance;
    }

    public static void setServer(String server)
    {
        ACContext.server = server;
    }

    public static void setToken(String token)
    {
        ACContext.token = token;
    }

    private static String buildUrl(String path) {
        return buildUrl(path, true);
    }

    private static String buildUrl(String path, boolean json) {
        String url;
        if (json) {
            url = path + "&auth_api_token=" + token + "&format=json";
        } else {
            url = path + "&auth_api_token=" + token;
        }
        return url;
    }

    public static Object processGet(String path) throws IOException
    {
        return JsonRest.processGet(buildUrl(path));
    }

    public static JSONObject processGetSingleJSONObj(String path) throws IOException {
        JSONObject toReturn = null;

        JSONArray jsonArr = (JSONArray) JsonRest.processGet(buildUrl(path));
        if (jsonArr != null) {
            if (jsonArr.size() > 1)
                throw new Error("Returned too many JSONObjects");
            return (JSONObject) jsonArr.get(0);
        }
        return toReturn;
    }

    public static JSONObject processPost(String postData, String path) throws IOException {
        //let's add the submitted=submitted that makes it work
        String toUse = postData + "&submitted=submitted";
        return (JSONObject) JsonRest.processPost(toUse, buildUrl(path));
    }

    /**
     * 
     * @return a basic string with: schema://servername/appropriatePath/api.php?path_info=pathToAppend
     */
    public static String getBasicUrlForPath(String pathToAppend) {
        return "https://" + server + "/ac/api.php?path_info=" + pathToAppend;

    }

    public static Set<ACProjectLabel> getACProjectLabels() throws IOException {
        Set<ACProjectLabel> projectLabels = new HashSet<ACProjectLabel>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet("https://" + server + "/ac/api.php?path_info=info/labels/project");
        for (Object object : jsonArr) {
            JSONObject jsonObj = (JSONObject) object;
            projectLabels.add(new ACProjectLabel(jsonObj));
        }
        return projectLabels;

    }

    public static Set<ACTaskLabel> getACTaskLabels() throws IOException {
        Set<ACTaskLabel> taskLabels = new HashSet<ACTaskLabel>();
        JSONArray jsonArr =
                (JSONArray) ACContext.processGet("https://" + server + "/ac/api.php?path_info=info/labels/assignment");
        for (Object object : jsonArr) {
            JSONObject jsonObj = (JSONObject) object;
            taskLabels.add(new ACTaskLabel(jsonObj));
        }
        return taskLabels;

    }

    public static List<ACProject> getActiveProjects() throws IOException {
        List<ACProject> projects = new ArrayList<ACProject>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet("https://" + server + "/ac/api.php?path_info=projects");
        if (jsonArr != null) {
            for (int i = 0; i < jsonArr.size(); i++)
                projects.add(new ACProject((JSONObject) jsonArr.get(i)));
        }
        return projects;

    }

    public static List<ACProject> getArchivedProjects() throws IOException {
        List<ACProject> projects = new ArrayList<ACProject>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet("https://" + server + "/ac/api.php?path_info=projects/archive");
        if (jsonArr != null) {
            for (int i = 0; i < jsonArr.size(); i++)
                projects.add(new ACProject((JSONObject) jsonArr.get(i)));
        }
        return projects;

    }

    /**
     * 
     * @return the 'projects' and the 'projects/archived' projects
     * @throws IOException
     */
    public static List<ACProject> getProjects() throws IOException
    {
        List<ACProject> allProjects = new ArrayList<>();
        allProjects.addAll(getActiveProjects());
        allProjects.addAll(getArchivedProjects());
        return allProjects;
    }

    public static String getServer() {
        return server;
    }

}
