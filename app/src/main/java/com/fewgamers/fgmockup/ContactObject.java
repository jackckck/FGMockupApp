package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class ContactObject {
    private String uuid, username, email, firstName, lastName, status, relationStatus;

    public ContactObject() {
        this.uuid = "";
        this.username = "";
        this.email = "";
        this.firstName = "";
        this.lastName = "";
        this.status = "";
    }

    public void defineContact(String contactString, String relationStatus) {
        Log.d("contactString", contactString);
        this.relationStatus = relationStatus;
        try {
            JSONObject thisContact = new JSONArray(contactString).getJSONObject(0);
            try {
                this.uuid = thisContact.getString("uuid");
                this.username = thisContact.getString("nickname");
                this.status = thisContact.getString("status");
            } catch (JSONException exception) {
                Log.e("Corrupt friend", "Friend data incomplete");
            }
            try {
                this.email = thisContact.getString("email");
            } catch (JSONException exception) {
                Log.d("Email private", this.uuid + "'s email is private");
                this.email = "";
            }
            try {
                this.firstName = thisContact.getString("firstname");
                this.lastName = thisContact.getString("lastname");
            } catch (JSONException exception) {
                Log.d("Name private", this.uuid + "'s name is private");
                this.firstName = "";
                this.lastName = "";
            }
        } catch (JSONException exception) {
            Log.e("User string error", "User string could not be formatted to JSONobject");
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

    public String getRelationStatus() {
        return relationStatus;
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
