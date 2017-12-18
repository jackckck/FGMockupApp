package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 12/16/2017.
 */

public class ChatObject {
    private Date date;
    private String message, user, dateString, timeOfDayString;
    private String messageComesFomMe;

    public void defineChatObject(JSONObject jsonObject) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            this.message = jsonObject.getString("message");
            this.user = jsonObject.getString("user");
            this.messageComesFomMe = jsonObject.getString("fromMe");
        } catch (JSONException exception) {
            Log.e("Corrupt chat object", "Some chat data missing");
        }
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }

    public String getDateString() {
        return dateString;
    }

    public String getTimeOfDayString() {
        return timeOfDayString;
    }

    public String isMessageFromMe() {
        return messageComesFomMe;
    }
}
