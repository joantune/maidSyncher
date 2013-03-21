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

import org.json.simple.JSONObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 15 de Fev de 2013
 *
 * 
 */
public class ACUser extends ACObject {

    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private int roleId;
    private int companyId;

    public ACUser(JSONObject jsonObj) throws IOException {
        super(jsonObj);
    }

    @Override
    protected void init(JSONObject jsonObject) throws IOException {
        setName(JsonRest.getString(jsonObject, "name"));
        setFirstName(JsonRest.getString(jsonObject, "first_name"));
        setLastName(JsonRest.getString(jsonObject, "last_name"));
        setEmail(JsonRest.getString(jsonObject, "email"));
        setRoleId(JsonRest.getInt(jsonObject, "role_id"));
        setCompanyId(JsonRest.getInt(jsonObject, "company_id"));

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

}
