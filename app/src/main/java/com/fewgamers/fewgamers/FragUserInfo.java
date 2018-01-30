package com.fewgamers.fewgamers;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/24/2017.
 */

// fragment that needs to be given a user's uuid. that user's user data is then displayed in the
// fragment once it is opened
public class FragUserInfo extends ListFragment {
    String uuid;
    TextView usernameDisplay, emailDisplay, firstNameDisplay, lastNameDisplay, serverListOwnership;

    ArrayList<ServerObject> serverList;
    ServerListAdapter userInfoServerListAdapter;

    public void setUserUUID(String friendUUID) {
        this.uuid = friendUUID;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraguserinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // retrieving widgets
        usernameDisplay = (TextView) getActivity().findViewById(R.id.userinfo_username_display);
        emailDisplay = (TextView) getActivity().findViewById(R.id.userinfo_email_display);
        firstNameDisplay = (TextView) getActivity().findViewById(R.id.userinfo_firstname_display);
        lastNameDisplay = (TextView) getActivity().findViewById(R.id.userinfo_lastname_display);
        serverListOwnership = (TextView) getActivity().findViewById(R.id.userInfoListOwnershipText);

        requestAndDisplayUserServerlist();

        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        requestUserData(requestQueue);
    }

    // clicking one of the user's servers will open an instance of FragServerInfo that displays the
    // clicked server
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FragServerBrowser.openServerInfo((ServerObject) l.getItemAtPosition(position), ((MainActivity) getActivity()));
    }

    // retrieves all non-private userdata from the given uuid, and displays that data in the
    // fragment's TextViews
    private void requestUserData(RequestQueue requestQueue) {
        String url = "https://fewgamers.com/api/user/?uuid=" + uuid + "&key=" + ((MainActivity) getActivity()).getMyKey();
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
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

    // uses a JSONArray containing user data to fill out the fragment's TextViews
    private void setDisplaysText(String userDataString) {
        ContactObject contactObject = new ContactObject(userDataString, null);
        String[] displayTexts = new String[4];
        displayTexts[0] = contactObject.getUsername();
        displayTexts[1] = contactObject.getEmail();
        displayTexts[2] = contactObject.getFirstName();
        displayTexts[3] = contactObject.getLastName();

        // all missing fields are inticated as private
        for (int i = 0; i < 4; i++) {
            if (displayTexts[i].equals("")) {
                displayTexts[i] = "Private";
            }
        }

        usernameDisplay.setText(displayTexts[0]);
        emailDisplay.setText(displayTexts[1]);
        firstNameDisplay.setText(displayTexts[2]);
        lastNameDisplay.setText(displayTexts[3]);
        // displayed above the user's list of servers
        serverListOwnership.setText(displayTexts[0] + "'s servers:");
    }

    // requests this user's list of servers, and displays them onscreen
    private void requestAndDisplayUserServerlist() {
        RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
        String url = "https://fewgamers.com/api/server/?creator=" + uuid + "&key=" + ((MainActivity) getActivity()).getMyKey();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                serverList = FragUser.getMyServerListFromJSONArray(response);
                userInfoServerListAdapter = new ServerListAdapter(getActivity(), serverList);
                setListAdapter(userInfoServerListAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
    }
}
