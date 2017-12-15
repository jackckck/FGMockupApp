package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.ListFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 12/6/2017.
 */

// hier komt de server browser. momenteel maakt hij al contact met onze server, maar hij levert nog niets zinnigs op
public class FragServerBrowser extends ListFragment {

    ArrayList<ServerObject> serverList, noFilterList;

    ServerListAdapter serverAdapter;

    EditText searchBar;
    ImageButton searchButton, sortingButton;

    Boolean sortingDirectionIsDown = true;

    Drawable upArrow, downArrow;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragserverbrowser, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // hier wordt een nieuwe requestview geïnstantieerd als die nog niet bestaat. als hij wel bestaat wordt de bestaande
        // requestview opgehaald.
        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        makeStringRequest(requestQueue, "http://www.fewgamers.com/api.php");

        String jsonString = "[{\"game\":\"CoD 2\",\"serverName\":\"Server One\",\"playercount\":\"8/10\",\"ip\":\"192.168.62.3\"},\n" +
                "{\"game\":\"DoD\", \"serverName\":\"Server Two\",\"playercount\":\"9/22\",\"ip\":\"192.168.2.1\"},\n" +
                "{\"game\":\"SC:BW\", \"serverName\":\"Server Three\",\"playercount\":\"2/4\",\"ip\":\"190.68.1.0\"}]";

        serverList = makeServerList(jsonString);
        noFilterList = makeServerList(jsonString);

        serverAdapter = new ServerListAdapter(getActivity(), serverList);
        setListAdapter(serverAdapter);

        searchBar = (EditText) getActivity().findViewById(R.id.serverSearchBar);
        searchButton = (ImageButton) getActivity().findViewById(R.id.serverSearchButton);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchFilter(s.toString().toLowerCase());
            }
        });

        sortingButton = (ImageButton) getActivity().findViewById(R.id.sortingButton);

        sortingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sortingDirectionIsDown) {
                    sortingButton.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
                    sortingDirectionIsDown = false;
                }
                else {
                    sortingButton.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                    sortingDirectionIsDown = true;
                }
                Collections.reverse(serverList);
                serverAdapter.notifyDataSetChanged();
            }
        });

        super.onViewCreated(view, savedInstanceState);

    }

    private void searchFilter(String search) {
        serverAdapter.clear();

        for (int i = 0; i < noFilterList.size(); i++) {
            ServerObject so = noFilterList.get(i);
            if (so.getServerName().toLowerCase().contains(search)) {
                serverAdapter.add(so);
            }
        }
    }

    private void makeStringRequest(RequestQueue queue, String url) {

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                TextView tV = (TextView) getActivity().findViewById(R.id.serverBrowserTextView);
                tV.setText("Response is: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView tV = (TextView) getActivity().findViewById(R.id.serverBrowserTextView);
                tV.setText("There was an error.");
            }
        });

        queue.add(stringRequest);
    }

    private ArrayList<ServerObject> makeServerList(String s) {
        JSONArray jsonArray = null;
        ArrayList<ServerObject> resList = new ArrayList<>();
        try {
            jsonArray = new JSONArray(s);
        } catch (JSONException exception) {
            Log.e("Server list not found", "Something when loading server data");
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                ServerObject serverObject = new ServerObject();

                serverObject.defineServer(jsonArray.getJSONObject(i));
                resList.add(serverObject);
            } catch (JSONException exception) {
                Log.e("Server object missing", "Server object data incomplete");
            }
        }

        return resList;
    }
}
