package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 1/20/2018.
 */

// class that updates the FewGamers database with a user's current device token for FireBase
// notifications
public class FGFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // get updated InstanceID token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase token", "Refreshed token: " + refreshedToken);

        // if the token refreshes, and the user is currently logged in (even if the app is not
        // running), and the user has notifications enabled, the updated token is sent to the server
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean notificationsAllowed = defaultPreferences.getBoolean(getResources().getString(R.string.pref_notifications_allow_notifications_key), true);
        SharedPreferences loginSharedPreferences = getApplicationContext().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        if (loginSharedPreferences.getBoolean("isLoggedIn", false) && notificationsAllowed) {
            sendRegistrationToServer(loginSharedPreferences.getString("uuid", null), refreshedToken, getResources().getString(R.string.master));
        }

    }

    public static void sendRegistrationToServer(String uuid, String token, String master) {
        try {
            JSONObject userdata = new JSONObject();
            JSONObject finalChangeQuery = new JSONObject();
            userdata.put("uuid", uuid);
            userdata.put("pushkey", token);
            finalChangeQuery.put("key", master);
            finalChangeQuery.put("userdata", new FGEncrypt().encrypt(userdata.toString()));
            Log.d("userdata unencrypted", userdata.toString());
            Log.d("final change", finalChangeQuery.toString());
            new FGAsyncTask().execute("https://fewgamers.com/api/user/", finalChangeQuery.toString(), "PATCH");
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }
}
