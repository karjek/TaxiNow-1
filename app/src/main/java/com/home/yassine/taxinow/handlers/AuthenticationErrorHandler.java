package com.home.yassine.taxinow.handlers;

import android.content.Intent;
import com.home.yassine.taxinow.LoginActivity;
import com.home.yassine.taxinow.SearchActivity;
import org.json.JSONObject;

/**
 * Created by Yassine on 20/09/2016.
 */
public class AuthenticationErrorHandler implements IMessageHandler {

    @Override
    public void HandleMessage(SearchActivity client, JSONObject jsonReader) {
        client.startActivity(new Intent(client, LoginActivity.class));
    }
}
