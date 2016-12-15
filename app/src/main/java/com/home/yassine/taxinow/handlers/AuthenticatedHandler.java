package com.home.yassine.taxinow.handlers;

import com.home.yassine.taxinow.SearchActivity;
import org.json.JSONObject;

/**
 * Created by Yassine on 20/09/2016.
 */
public class AuthenticatedHandler implements IMessageHandler {

    @Override
    public void HandleMessage(SearchActivity client, JSONObject jsonReader) {
        client.mConnectionState = SearchActivity.ConnectionState.CONNECTED;
    }
}
