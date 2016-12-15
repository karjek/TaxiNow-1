package com.home.yassine.taxinow.handlers;

import com.home.yassine.taxinow.SearchActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 24/09/2016.
 */
public class DriverCloseHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final SearchActivity client, JSONObject jsonReader) throws JSONException {

        if (client.Stopped && client.mSearchState == SearchActivity.SearchState.WAITING) {
            client.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    client.pushNotification("Votre taxi est proche", SearchActivity.class);
                }
            });
        }
    }
}
