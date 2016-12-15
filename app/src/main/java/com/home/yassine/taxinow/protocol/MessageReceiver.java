package com.home.yassine.taxinow.protocol;

import android.util.Log;
import com.home.yassine.taxinow.SearchActivity;
import com.home.yassine.taxinow.handlers.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Yassine on 15/09/2016.
 */
public class MessageReceiver {

    private HashMap<Long, IMessageHandler> Handlers = new HashMap<>();

    public MessageReceiver() {

        Handlers.put((long) 1000, new ErrorHandler());
        Handlers.put((long) 1002, new AuthenticatedHandler());
        Handlers.put((long) 1003, new AuthenticationErrorHandler());
        Handlers.put((long) 200, new DriverFoundAndAcceptedHandler());
        Handlers.put((long) 201, new DriverLocationUpdateHandler());
        Handlers.put((long) 202, new WaitingDriverDisconnectedHandler());
        Handlers.put((long) 203, new DriverCloseHandler());
        Handlers.put((long) 300, new RefreshTaxiChoiceListHandler());
    }

    public void Receive(SearchActivity activity, String message) {

        try {
            JSONObject jo = new JSONObject(message);
            long messageId = jo.getLong("messageId");
            Handlers.get(messageId).HandleMessage(activity, jo);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR_JSON_PARSE", message);
        }
    }
}
