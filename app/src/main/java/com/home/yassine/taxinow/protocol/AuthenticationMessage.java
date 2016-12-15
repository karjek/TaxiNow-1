package com.home.yassine.taxinow.protocol;

import com.home.yassine.taxinow.types.LocationData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 20/09/2016.
 */
public class AuthenticationMessage extends WebSocketMessage {

    public String email;
    public String token;
    public String manufacturer;
    public String model;
    public LocationData destination;
    public LocationData currentLocation;
    public String destAddress;
    public String locAddress;
    public int numRiders;
    public boolean detour;
    public boolean femaleOnly;
    public boolean isUserChoosingTaxi;

    public AuthenticationMessage() {
        super(100);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {

        throw new UnsupportedOperationException();
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {

        super.pack(jsonWriter);

        jsonWriter.put("email", email);
        jsonWriter.put("token", token);
        jsonWriter.put("manufacturer", manufacturer);
        jsonWriter.put("model", model);
        jsonWriter.put("destAddress", destAddress);
        jsonWriter.put("locAddress", locAddress);
        jsonWriter.put("numRiders", numRiders);
        jsonWriter.put("detour", detour);
        jsonWriter.put("femaleOnly", femaleOnly);
        jsonWriter.put("userChoice", isUserChoosingTaxi);

        JSONObject locationWriter = new JSONObject();

        destination.pack(locationWriter);

        jsonWriter.put("dest", locationWriter.toString());

        currentLocation.pack(locationWriter);

        jsonWriter.put("curr", locationWriter.toString());
    }
}
