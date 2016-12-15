package com.home.yassine.taxinow.types;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by Yassine on 20/09/2016.
 */
public class LocationData implements INetworkType {

    public double Long;
    public double Lat;
    public float Acc;


    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {

        Long = jsonReader.getDouble("long");
        Lat = jsonReader.getDouble("lat");
        Acc = BigDecimal.valueOf(jsonReader.getDouble("acc")).floatValue();
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        jsonWriter.put("long", Long);
        jsonWriter.put("lat", Lat);
        jsonWriter.put("acc", Acc);
    }
}
