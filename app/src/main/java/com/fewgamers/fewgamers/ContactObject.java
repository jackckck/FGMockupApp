package com.fewgamers.fewgamers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

// instances of this class are used to represent users in an ArrayList<ContactObject>, which in turn
// is translated by an instance of ContactsListAdapter to display a ListView of contacts onscreen
public class ContactObject {
    // fields for all of a user's userdata
    private String uuid, username, email, firstName, lastName, accountStatus, relationStatus;
    // used to indicate a special contact object, used to separate incoming and outgoing friend
    // requests
    private boolean isHeader;
    private String header;

    // constructor that uses a given JSONString containing all non-private userdata to set its fields
    public ContactObject(String contactString, String relationStatus) {
        this.relationStatus = relationStatus;
        try {
            // contactString will look like "[{"uuid":uuid, ...}]
            JSONObject thisContact = new JSONArray(contactString).getJSONObject(0);
            try {
                this.uuid = thisContact.getString("uuid");
                this.username = thisContact.getString("nickname");
                this.accountStatus = thisContact.getString("status");
                try {
                    this.email = thisContact.getString("email");
                } catch (JSONException exception) {
                    this.email = "private";
                    Log.d("email private", this.username + "'s email is private.");
                    exception.printStackTrace();
                }
                try {
                    this.firstName = thisContact.getString("firstname");
                    this.lastName = thisContact.getString("lastname");
                } catch (JSONException exception) {
                    this.firstName = "private";
                    this.lastName = "private";
                    Log.d("names private", this.username + "'s first and last name are private.");
                    exception.printStackTrace();
                }
            } catch (JSONException exception) {
                Log.e("Corrupt friend", "Friend data incomplete");
                exception.printStackTrace();
            }
        } catch (JSONException exception) {
            Log.e("User string error", "User string could not be formatted to JSONObject.");
            exception.printStackTrace();
        } catch (NullPointerException exception) {
            // this ContactObject is a header
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
