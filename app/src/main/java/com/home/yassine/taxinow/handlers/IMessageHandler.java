package com.home.yassine.taxinow.handlers;

import com.home.yassine.taxinow.SearchActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 15/09/2016.
 */
public interface IMessageHandler {

    void HandleMessage(SearchActivity client, JSONObject jsonReader) throws JSONException;
}
