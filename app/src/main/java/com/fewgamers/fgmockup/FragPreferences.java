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

// de settings fragment. hier zit op het moment alleen nog night mode als werkende setting in
public class FragPreferences extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

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
            userdata.put("uuid", ((MainActivity) getActivity()).uuid);
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

    private void blockNotifications(boolean allowed) {
        FGEncrypt fgEncrypt = new FGEncrypt();

        String pushkey;
        if (allowed) {
            pushkey = FirebaseInstanceId.getInstance().getToken();
        } else {
            pushkey = "None";
        }
        try {
            JSONObject userdata = new JSONObject();
            userdata.put("uuid", ((MainActivity) getActivity()).uuid);
            userdata.put("pushkey", pushkey);
            String encryptedUserdata = fgEncrypt.encrypt(userdata.toString());

            Log.d("userdata unencrypted", userdata.toString());

            String finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, getResources().getString(R.string.master));

            new FGAsyncTask().execute("https://fewgamers.com/api/user/", finalQuery, "PATCH");
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }
}
