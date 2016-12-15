package com.home.yassine.taxinow.handlers;

import com.home.yassine.taxinow.SearchActivity;
import com.home.yassine.taxinow.protocol.RefreshTaxiChoiceListMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 01/10/2016.
 */
public class RefreshTaxiChoiceListHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final SearchActivity client, JSONObject jsonReader) throws JSONException {

        final RefreshTaxiChoiceListMessage message = new RefreshTaxiChoiceListMessage();

        message.unpack(jsonReader);

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                client.refreshTaxiChoiceList(message.taxiInfos);
            }
        });
    }
}
