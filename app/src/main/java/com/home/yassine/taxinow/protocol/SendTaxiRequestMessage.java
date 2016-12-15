package com.home.yassine.taxinow.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 01/10/2016.
 */
public class SendTaxiRequestMessage extends WebSocketMessage {

    public String driverId;

    public SendTaxiRequestMessage() {
        super(301);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        driverId = jsonReader.getString("driverId");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("driverId", driverId);
    }
}
