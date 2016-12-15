package com.home.yassine.taxinow.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 20/09/2016.
 */
public class WebSocketMessage {
    protected long messageId;

    WebSocketMessage(long messageId) {
        this.messageId = messageId;
    }

    public void unpack(JSONObject jsonReader) throws JSONException {
        messageId = jsonReader.getLong("messageId");
    }

    public void pack(JSONObject jsonWriter)  throws JSONException {
        jsonWriter.put("messageId", messageId);
    }

    public long getMessageId() {
        return messageId;
    }
}
