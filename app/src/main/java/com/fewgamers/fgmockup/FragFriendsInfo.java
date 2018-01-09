package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/24/2017.
 */

public class FragFriendsInfo extends Fragment {

    String uuid;
    TextView usernameDisplay, emailDisplay, firstNameDisplay, lastNameDisplay;

    public void setFriendUUID(String friendUUID) {
        this.uuid = removeHyphens(friendUUID);
    }

    private String removeHyphens(String uuid) {
        String res = uuid;
        res.replace("-", "");
        return res;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraguserinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoUsernameDisplay);
        emailDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoEMailDisplay);
        firstNameDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoFirstNameDisplay);
        lastNameDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoLastNameDisplay);

        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        requestUserData(requestQueue, uuid);
    }

    private void requestUserData(RequestQueue requestQueue, String uuid) {
        String urlString = "https://fewgamers.com/api/user/?uuid=" + uuid;
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setDisplaysText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("/api/user/ error", error.toString());
            }
        });

        requestQueue.add(stringRequest);
    }

    private void setDisplaysText(String userDataString) {
        try {
            JSONObject userDataObject = new JSONObject(userDataString);

            usernameDisplay.setText(userDataObject.getString("nickname"));
            emailDisplay.setText(userDataObject.getString("email"));
            firstNameDisplay.setText(userDataObject.getString("firstname"));
            lastNameDisplay.setText(userDataObject.getString("lastname"));
        } catch (JSONException exception) {
            Log.e("User data error", "Could not extract user data from server response.");
        }
    }
}
