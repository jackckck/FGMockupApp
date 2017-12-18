package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class FriendObject {
    private String friendName, friendEMail, friendStatus;

    public void defineFriend(JSONObject jsonObject) {
        try {
            this.friendName = jsonObject.getString("name");
            this.friendEMail = jsonObject.getString("email");
            this.friendStatus = jsonObject.getString("status");
        } catch (JSONException j) {
            Log.e("Corrupt friend", "Friend data incomplete");
        }
    }

    public String getFriendName() {
        return friendName;
    }
    public String getFriendEMail() {
        return friendEMail;
    }
    public String getFriendStatus() {
        return friendStatus;
    }
}
