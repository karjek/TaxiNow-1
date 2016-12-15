package com.home.yassine.taxinow.handlers;

import com.home.yassine.taxinow.SearchActivity;
import com.home.yassine.taxinow.protocol.DriverLocationUpdateMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 */
public class DriverLocationUpdateHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final SearchActivity client, JSONObject jsonReader) throws JSONException {

        final DriverLocationUpdateMessage message = new DriverLocationUpdateMessage();

        message.unpack(jsonReader);

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                client.UpdateDriverPosition(message.lat, message.lon);
            }
        });
    }
}
