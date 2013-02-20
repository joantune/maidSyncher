package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        String url = path + "&auth_api_token=" + token + "&format=json";
        return url;
    }

    public static Object processGet(String path) throws IOException
    {
        return JsonRest.processGet(buildUrl(path));
    }

    public static List<ACProject> getProjects() throws IOException
    {
        List<ACProject> projects = new ArrayList<ACProject>();
        JSONArray jsonArr = (JSONArray) ACContext.processGet("https://" + server + "/ac/api.php?path_info=projects");
        if(jsonArr != null) {
            for(int i = 0; i < jsonArr.size(); i++)
                projects.add(new ACProject((JSONObject)jsonArr.get(i)));
        }
        return projects;
    }

    public static String getServer() {
        return server;
    }

}
