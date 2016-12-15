package com.home.yassine.taxinow.protocol;

import com.home.yassine.taxinow.types.TaxiInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yassine on 01/10/2016.
 */
public class RefreshTaxiChoiceListMessage extends WebSocketMessage {

    public ArrayList<TaxiInfo> taxiInfos;

    public RefreshTaxiChoiceListMessage() {
        super(300);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {

        super.unpack(jsonReader);

        JSONArray array = jsonReader.getJSONArray("taxiInfos");

        taxiInfos = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {

            JSONObject jo = array.getJSONObject(i);

            TaxiInfo taxiInfo = new TaxiInfo();

            taxiInfo.unpack(jo);

            taxiInfos.add(taxiInfo);
        }
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        throw new UnsupportedOperationException();
    }
}
