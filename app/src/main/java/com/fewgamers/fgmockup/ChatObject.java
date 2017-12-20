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
    private String message, user;
    private String messageComesFomMe;

    public void defineChatObject(String user, String message, String fromMe) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        this.user = user;
        this.message = message;
        this.messageComesFomMe = fromMe;
    }

    public String getMessage() {
        return message;
    }

    public String isMessageFromMe() {
        return messageComesFomMe;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageComesFomMe(String fromMe) {
        this.messageComesFomMe = fromMe;
    }
}
