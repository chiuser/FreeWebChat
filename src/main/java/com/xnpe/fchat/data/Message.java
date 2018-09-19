package com.xnpe.fchat.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Message implements MessageKey {
    private String roomId;
    private String name;
    private String command;
    private String info;

    public Message(String json) {
        if (json != null && json.length() > 0) {
            JSONObject jsonObject = new JSONObject(json);
            this.roomId = jsonObject.optString(KEY_ROOM_ID);
            this.command = jsonObject.optString(KEY_MESSAGE_COMMAND);
            this.info = jsonObject.optString(KEY_INFO);
            this.name = jsonObject.optString(KEY_WEBSOCKET_USERNAME);
        }
    }

    public JSONObject toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_INFO, this.info);
            jsonObject.put(KEY_WEBSOCKET_USERNAME, this.name);
            jsonObject.put(KEY_MESSAGE_COMMAND, this.command);
            jsonObject.put(KEY_ROOM_ID, this.roomId);
            return jsonObject;
        } catch (JSONException e) {

        }
        return null;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    public String getInfo() {
        return info;
    }
}
