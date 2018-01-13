package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class ContactObject {
    private String uuid, username, email, firstName, lastName, status;

    public ContactObject() {
        this.uuid = "";
        this.username = "";
        this.email = "";
        this.firstName = "";
        this.lastName = "";
        this.status = "";
    }

    public void defineContact(String contactString) {
        try {
            JSONObject thisContact = new JSONObject(contactString);
            this.uuid = thisContact.getString("uuid");
            this.username = thisContact.getString("nickname");
            this.email = thisContact.getString("email");
            this.firstName = thisContact.getString("firstname");
            this.lastName = thisContact.getString("lastname");
            this.status = thisContact.getString("status");
        } catch (JSONException exception) {
            Log.e("Corrupt friend", "Friend data incomplete");
        }
    }

    public String getUsername() {
        return username;
    }

    public String getUUID() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
