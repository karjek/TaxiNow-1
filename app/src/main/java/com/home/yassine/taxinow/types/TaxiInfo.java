package com.home.yassine.taxinow.types;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yassine on 29/09/2016.
 */
public class TaxiInfo implements INetworkType {

    public String driverId;
    public double lat;
    public double lon;
    public ArrayList<String> passengers;

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {

        driverId = jsonReader.getString("driverId");
        lat = jsonReader.getDouble("lat");
        lon = jsonReader.getDouble("lon");

        JSONArray array = jsonReader.getJSONArray("passengers");

        passengers = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            passengers.add(array.getString(i));
        }
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        throw new UnsupportedOperationException();
    }
}
