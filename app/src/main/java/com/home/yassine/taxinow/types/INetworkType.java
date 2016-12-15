package com.home.yassine.taxinow.types;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 20/09/2016.
 */
public interface INetworkType {
    void unpack(JSONObject jsonReader) throws JSONException;
    void pack(JSONObject jsonWriter) throws JSONException;
}
