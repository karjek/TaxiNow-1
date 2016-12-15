package com.home.yassine.taxinow;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 12/09/2016.
 */
public class User {

    private String Email;
    private String FirstName;
    private String LastName;
    private String Token;
    private String Gender;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String ToJsonString() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", this.Email);
        jsonObject.put("firstName", this.FirstName);
        jsonObject.put("lastName", this.LastName);
        jsonObject.put("token", this.Token);
        jsonObject.put("gender", this.Gender);

        return jsonObject.toString();
    }

    public static User FromJson(JSONObject jsonObject) throws JSONException {

        User user = new User();

        user.setEmail(jsonObject.getString("email"));
        user.setFirstName(jsonObject.getString("firstName"));
        user.setLastName(jsonObject.getString("lastName"));
        user.setToken(jsonObject.getString("token"));
        user.setGender(jsonObject.getString("gender"));

        return user;
    }

    public JSONObject ToJson() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", this.Email);
        jsonObject.put("firstName", this.FirstName);
        jsonObject.put("lastName", this.LastName);
        jsonObject.put("token", this.Token);
        jsonObject.put("gender", this.Gender);

        return jsonObject;
    }
}
