package com.home.yassine.taxinow.handlers;

import com.home.yassine.taxinow.SearchActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 23/09/2016.
 */
public class WaitingDriverDisconnectedHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final SearchActivity client, JSONObject jsonReader) throws JSONException {

        client.pushNotification("Le taxi a annulé votre demande !", SearchActivity.class);

        client.BackToSearch();
    }
}
