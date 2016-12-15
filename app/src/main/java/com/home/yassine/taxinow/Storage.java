package com.home.yassine.taxinow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 12/09/2016.
 */
public class Storage {

    public static void SaveUser(Activity context, User user) throws JSONException {

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("User", user.ToJsonString());
        editor.apply();
    }

    public static User LoadUser(Activity context) {

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        String UserJson = sharedPref.getString("User", "");

        if (UserJson.equals(""))
            return null;

        User u = null;

        try {
            u = User.FromJson(new JSONObject(UserJson));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return u;
    }
}
