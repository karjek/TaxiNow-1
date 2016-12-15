package com.home.yassine.taxinow.protocol;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 */
public class DriverFoundAndAcceptedMessage extends WebSocketMessage {

    public double lat;
    public double lon;

    public DriverFoundAndAcceptedMessage() {
        super(200);
    }

    public DriverFoundAndAcceptedMessage(long messageId) {
        super(messageId);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        lat = jsonReader.getDouble("lat");
        lon = jsonReader.getDouble("lon");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("lat", lat);
        jsonWriter.put("lon", lon);
    }
}
