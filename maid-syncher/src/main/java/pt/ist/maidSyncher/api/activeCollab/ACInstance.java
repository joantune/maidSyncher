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

    private String name;
    private String classType;

    public ACInstance(JSONObject jsonObj) throws IOException {
        super(jsonObj);
    }

    public static ACInstance getInstanceForCompanyName(String companyName) throws IOException {

        JSONArray jsonArr =
                (JSONArray) getRequestProcessor().processGet(
                        ACContext.getInstance().getServerBaseUrl()
                        + "/api.php?path_info=people");
        if (jsonArr != null) {
            for (Object object : jsonArr) {
                JSONObject jsonObj = (JSONObject) object;
                if (JsonRest.getString(jsonObj, "name").equalsIgnoreCase(companyName)) {
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
        JSONArray jsonArray = (JSONArray) getRequestProcessor().processGet(getUrl() + "/users");
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
