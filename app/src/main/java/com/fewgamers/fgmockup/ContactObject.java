package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class ContactObject {
    private String uuid, username, email, firstName, lastName, accountStatus, relationStatus;
    private boolean isHeader;
    private String header;

    public ContactObject() {
        this.uuid = "";
        this.username = "";
        this.email = "";
        this.firstName = "";
        this.lastName = "";
        this.accountStatus = "";
    }

    public void defineContact(String contactString, String relationStatus) {
        Log.d("contactString", contactString);
        this.relationStatus = relationStatus;
        try {
            JSONObject thisContact = new JSONArray(contactString).getJSONObject(0);
            try {
                this.uuid = thisContact.getString("uuid");
                this.username = thisContact.getString("nickname");
                this.accountStatus = thisContact.getString("status");
                try {
                    this.email = thisContact.getString("email");
                } catch (JSONException exception) {
                    Log.d("email private", this.username + "'s email is private.");
                }
                try {
                    this.firstName = thisContact.getString("firstname");
                    this.lastName = thisContact.getString("lastname");
                } catch (JSONException exception) {
                    Log.d("names private", this.username + "'s first and last name are private.");
                }
            } catch (JSONException exception) {
                Log.e("Corrupt friend", "Friend data incomplete");
            }
        } catch (JSONException exception) {
            Log.e("User string error", "User string could not be formatted to JSONobject");
        }
    }

    public void setAsHeader(String header) {
        this.isHeader = true;
        this.header = header;
    }

    public boolean isAHeader() {
        return isHeader;
    }

    public String getHeader() {
        return header;
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

    public String getAccountStatus() {
        return accountStatus;
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

    public void setAccountStatus(String status) {
        this.accountStatus = status;
    }
}
