package com.fewgamers.fewgamers;

/**
 * Created by Administrator on 12/16/2017.
 */

// this class is currently not in use, due to the absence of a chat feature in our service
public class ChatObject {
    private String date, timeOfDay;
    private String message, user;
    private String messageComesFomMe;
    private boolean isDateNotifier;

    public void defineChatObject(String user, String message, String fromMe, String date, String time) {
        this.user = user;
        this.message = message;
        this.messageComesFomMe = fromMe;
        this.date = date;
        this.timeOfDay = time;
    }

    public String getMessage() {
        return message;
    }

    public String isMessageFromMe() {
        return messageComesFomMe;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public String getDate() {
        return date;
    }

    public boolean isADateNotifier() {
        return isDateNotifier;
    }


    public void setAsDateNotifer() {
        this.isDateNotifier = true;
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

    public void setDate(String dateString) {
        this.date = dateString;
    }
}
