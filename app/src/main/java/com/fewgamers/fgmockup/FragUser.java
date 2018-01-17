package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragUser extends ListFragBase {

    String myServersString, favServersString;
    ArrayList<ServerObject> userServerList, myServersList, favServersList;

    String uuid = "2532ef98f6034792baf22471073683f6";

    MainActivity mainActivity;

    ServerListAdapter userAdapter;

    TabLayout.OnTabSelectedListener userTabsSelect;

    TextView usernameText, emailText, firstNameText, lastNameText, usernameDisplay, emailDisplay, firstNameDisplay, lastNameDisplay;

    private boolean notReady;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraguserinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        // laden begint
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        usernameDisplay = mainActivity.findViewById(R.id.friendsInfoUsernameDisplay);
        emailDisplay = mainActivity.findViewById(R.id.friendsInfoEMailDisplay);
        firstNameDisplay = mainActivity.findViewById(R.id.friendsInfoFirstNameDisplay);
        lastNameDisplay = mainActivity.findViewById(R.id.friendsInfoLastNameDisplay);

        usernameText = mainActivity.findViewById(R.id.friendsInfoUsername);
        emailText = mainActivity.findViewById(R.id.friendsInfoEMail);
        firstNameText = mainActivity.findViewById(R.id.friendsInfoFirstName);
        lastNameText = mainActivity.findViewById(R.id.friendsInfoLastName);

        setUserDisplays();

        favServersList = new ArrayList<>();

        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // requestMyServers(requestQueue, mainActivity.uuid);
        requestMyServers(requestQueue, uuid);

        if (mainActivity.friendsTabSelected != 0) {
            toggleDisplaysVisibility(false);
        }

        userTabsSelect = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (notReady) {
                    return;
                }
                userServerList.clear();
                switch (tab.getPosition()) {
                    case 0:
                        mainActivity.userTabSelected = 0;
                        toggleDisplaysVisibility(true);
                        break;
                    case 1:
                        mainActivity.userTabSelected = 1;
                        userServerList.addAll(myServersList);
                        toggleDisplaysVisibility(false);
                        break;
                    case 2:
                        mainActivity.userTabSelected = 2;
                        userServerList.addAll(favServersList);
                        toggleDisplaysVisibility(false);
                        break;
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };
        mainActivity.friendsTabs.addOnTabSelectedListener(userTabsSelect);
    }

    private void toggleDisplaysVisibility(boolean b) {
        int v;
        if (b) {
            v = View.VISIBLE;
        } else {
            v = View.GONE;
        }
        usernameDisplay.setVisibility(v);
        emailDisplay.setVisibility(v);
        firstNameDisplay.setVisibility(v);
        lastNameDisplay.setVisibility(v);
        usernameText.setVisibility(v);
        emailText.setVisibility(v);
        firstNameText.setVisibility(v);
        lastNameText.setVisibility(v);
    }

    private void setUserDisplays() {
        usernameDisplay.setText(mainActivity.username);
        emailDisplay.setText(mainActivity.email);
        firstNameDisplay.setText(mainActivity.firstName);
        lastNameDisplay.setText(mainActivity.lastName);
    }

    private void requestMyServers(RequestQueue requestQueue, String uuid) {
        String urlString = "https://fewgamers.com/api/server/?uuid=" + uuid + mainActivity.urlKey;
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                myServersList = getMyServerListFromString(response);

                switch (mainActivity.userTabSelected) {
                    case 1:
                        userServerList = new ArrayList<>(myServersList);
                        break;
                    case 2:
                        userServerList = new ArrayList<>(favServersList);
                        break;
                    default:
                        userServerList = new ArrayList<>();
                }
                userAdapter = new ServerListAdapter(getActivity(), userServerList);
                setListAdapter(userAdapter);

                // laden is voorbij
                mainActivity.mainProgressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("/api/server/?uuid error", error.toString());
            }
        });

        requestQueue.add(stringRequest);
    }

    private ArrayList<ServerObject> getMyServerListFromString(String string) {
        ArrayList<ServerObject> res = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(string);

            for (int i = 0; i < jsonArray.length(); i++) {
                ServerObject serverObject = new ServerObject();
                serverObject.defineServer(jsonArray.getJSONObject(i));
                res.add(serverObject);
            }
        } catch (JSONException exception) {
            Log.e("Server error", "Couldn't extract server list from string");
        }
        return res;
    }
}
