package com.home.yassine.taxinow.handlers;

import com.home.yassine.taxinow.SearchActivity;
import com.home.yassine.taxinow.protocol.DriverFoundAndAcceptedMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 */
public class DriverFoundAndAcceptedHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final SearchActivity client, JSONObject jsonReader) throws JSONException {

        final DriverFoundAndAcceptedMessage message = new DriverFoundAndAcceptedMessage();

        message.unpack(jsonReader);

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                client.setupMap(message.lat, message.lon);
            }
        });
    }
}
