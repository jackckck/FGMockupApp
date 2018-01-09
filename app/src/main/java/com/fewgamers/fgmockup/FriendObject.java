package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class FriendObject {
    private String friendName, friendEMail, firstName, lastName, friendStatus;
    private Integer unreadCount;

    public FriendObject() {
        friendName = "";
        friendEMail = "";
        firstName = "";
        lastName = "";
        friendStatus = "";
    }

    public void defineFriend(JSONObject jsonObject, String myUsername) {
        try {
            String userOne, userTwo;
            userOne = jsonObject.getString("user1");
            userTwo = jsonObject.getString("user2");

            if (myUsername.equals(userOne)) {
                this.friendName = userTwo;
            } else if (myUsername.equals(userTwo)) {
                this.friendName = userOne;
            } else {
                return;
            }

            //this.friendEMail = jsonObject.getString("email");
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public void setFriendEMail(String friendEMail) {
        this.friendEMail = friendEMail;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    public void addToUnread(Integer count) {
        unreadCount += count;
    }
}
