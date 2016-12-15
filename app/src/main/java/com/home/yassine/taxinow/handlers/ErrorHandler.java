package com.home.yassine.taxinow.handlers;

import android.widget.Toast;
import com.home.yassine.taxinow.SearchActivity;
import com.home.yassine.taxinow.protocol.ErrorMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 17/09/2016.
 */
public class ErrorHandler implements IMessageHandler {
    @Override
    public void HandleMessage(final SearchActivity client, JSONObject jsonReader) {

        final ErrorMessage errorMessage = new ErrorMessage();

        try {
            errorMessage.unpack(jsonReader);
            client.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(client, errorMessage.error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
