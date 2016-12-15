package com.home.yassine.taxinow.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 15/09/2016.
 */
public class ErrorMessage extends WebSocketMessage {

    public String error;

    public ErrorMessage() {
        super(1000);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {

        super.unpack(jsonReader);

        error = jsonReader.getString("error");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {

        super.pack(jsonWriter);

        jsonWriter.put("error", error);
    }
}
