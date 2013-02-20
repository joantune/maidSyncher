/**
 * 
 */
package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 15 de Fev de 2013
 * 
 *         It is associated with the instance of ActiveCollab
 * 
 */
public class ACInstance extends ACObject {

    private final static String DEFAULT_COMPANY_NAME = "Direção dos Serviços de Informática (DSI)";

    private String name;
    private String classType;

    public ACInstance(JSONObject jsonObj) throws IOException {
        super(jsonObj);
    }

    public static ACInstance getInstanceForCompanyName() throws IOException {

        JSONArray jsonArr = (JSONArray) ACContext.processGet("https://" + ACContext.getServer() + "/ac/api.php?path_info=people");
        if (jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObj = (JSONObject) object;
                if (JsonRest.getString(jsonObj, "name").equalsIgnoreCase(DEFAULT_COMPANY_NAME)) {
                    return new ACInstance(jsonObj);
                }

            }
        }
        return null;

    }

    @Override
    protected void init(JSONObject jsonObject) throws IOException {
        setName(JsonRest.getString(jsonObject, "name"));
        setClassType(JsonRest.getString(jsonObject, "class"));

    }

    public List<ACUser> getUsers() throws IOException {
        List<ACUser> users = new ArrayList<ACUser>();
        JSONArray jsonArray = (JSONArray) ACContext.processGet(_url + "/users");
        for (Object object : jsonArray) {
            JSONObject jsonObj = (JSONObject) object;
            users.add(new ACUser(jsonObj));
        }

        return users;

    }

    @Override
    public String toJSONString() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

}
