package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/6/2017.
 */

// fragment for changing user preferences
public class FragPreferences extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    // when the settings screen is closed, all changed settings pertaining to privacy and notifications
    // are sent to the FewGamers database
    @Override
    public void onStop() {
        super.onStop();

        MainActivity mainActivity = (MainActivity) getActivity();
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean emailIsVisible = defaultPreferences.getBoolean(getResources().getString(R.string.pref_email_visible_key), true);
        boolean nameIsVisible = defaultPreferences.getBoolean(getResources().getString(R.string.pref_name_visible_key), true);

        this.makePrivate("email", emailIsVisible, nameIsVisible);

        boolean notificationsAreAllowed = defaultPreferences.getBoolean(getResources().getString(R.string.pref_notifications_allow_notifications_key), true);

        blockNotifications(notificationsAreAllowed);
    }

    // changes userdata to reflect the user's email visibility and name visibility
    private void makePrivate(String value, boolean emailIsVisible, boolean nameIsVisible) {
        String emailPrivate, namePrivate, encryptedUserdata;
        FGEncrypt fgEncrypt = new FGEncrypt();
        if (emailIsVisible) {
            emailPrivate = "False";
        } else {
            emailPrivate = "True";
        }
        if (nameIsVisible) {
            namePrivate = "False";
        } else {
            namePrivate = "True";
        }
        try {
            JSONObject userdata = new JSONObject();
            userdata.put("uuid", ((MainActivity) getActivity()).getMyUuid());
            userdata.put("emailprivate", emailPrivate);
            userdata.put("nameprivate", namePrivate);
            encryptedUserdata = fgEncrypt.encrypt(userdata.toString());

            Log.d("userdata unencrypted", userdata.toString());

            String finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, getResources().getString(R.string.master));
            Log.d("finalquery", finalQuery);

            new FGAsyncTask().execute("https://fewgamers.com/api/user/", finalQuery, "PATCH");
        } catch (JSONException exception) {
            Log.e("Userdata error", "Something went wrong when formatting JSON strings");
        }
    }

    // if notifications are not allowed, the user's FireBase token will be erased from the FewGamers
    // database
    private void blockNotifications(boolean allowed) {
        FGEncrypt fgEncrypt = new FGEncrypt();

        String pushKey;
        if (allowed) {
            pushKey = FirebaseInstanceId.getInstance().getToken();
        } else {
            pushKey = "None";
        }
        try {
            JSONObject userdata = new JSONObject();
            userdata.put("uuid", ((MainActivity) getActivity()).getMyUuid());
            userdata.put("pushkey", pushKey);
            String encryptedUserdata = fgEncrypt.encrypt(userdata.toString());

            Log.d("userdata unencrypted", userdata.toString());

            String finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, getResources().getString(R.string.master));

            new FGAsyncTask().execute("https://fewgamers.com/api/user/", finalQuery, "PATCH");
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }
}
