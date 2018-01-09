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
import android.widget.RelativeLayout;

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

public class FragUser extends ListFragment {

    String myServersString, favServersString;
    ArrayList<ServerObject> userServerList;

    String user = "082894cf2c7c4b3087ff806792804dfe";

    MainActivity mainActivity;

    ServerListAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraguserinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        userServerList = new ArrayList<>();

        userAdapter = new ServerListAdapter(getActivity(), userServerList);
        setListAdapter(userAdapter);

        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        requestMyServers(requestQueue, user);

        mainActivity.friendsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                userServerList.clear();
                switch (tab.getPosition()) {
                    case 0:
                        mainActivity.friendsTabSelected = 0;
                        break;
                    case 1:
                        getServerListFromString(myServersString);
                        mainActivity.friendsTabSelected = 1;
                        break;
                    case 2:
                        mainActivity.friendsTabSelected = 2;
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
        });
    }

    private void requestMyServers(RequestQueue requestQueue, String uuid) {
        String urlString = "https://fewgamers.com/api/server/?uuid=" + uuid;
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                myServersString = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("/api/server/?uuid error", error.toString());
            }
        });

        requestQueue.add(stringRequest);
    }

    private void getServerListFromString(String string) {
        try {
            JSONArray jsonArray = new JSONArray(string);

            for (int i = 0; i < jsonArray.length(); i++) {
                ServerObject serverObject = new ServerObject();
                serverObject.defineServer(jsonArray.getJSONObject(i));
                userServerList.add(serverObject);
            }
        } catch (JSONException exception) {
            Log.e("Server error", "Couldn't extract server list from string");
        }
    }
}
