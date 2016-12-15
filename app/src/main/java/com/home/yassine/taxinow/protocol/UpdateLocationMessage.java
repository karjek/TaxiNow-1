package com.home.yassine.taxinow.protocol;

import com.home.yassine.taxinow.types.LocationData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 20/09/2016.
 */
public class UpdateLocationMessage extends WebSocketMessage {

    public LocationData currentLocation;

    public UpdateLocationMessage() {
        super(102);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);
        currentLocation.unpack(jsonReader);
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);
        currentLocation.pack(jsonWriter);
    }
}
