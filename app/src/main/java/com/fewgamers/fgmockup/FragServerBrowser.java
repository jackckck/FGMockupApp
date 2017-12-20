package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 12/6/2017.
 */

// hier komt de server browser. momenteel maakt hij al contact met onze server, maar hij levert nog niets zinnigs op
public class FragServerBrowser extends ListFragment {
    ArrayList<ServerObject> serverList, serverListAlphabetical, serverListByPlayers;

    ServerListAdapter serverAdapter;

    EditText searchBar;
    ImageButton searchButton, sortingButton;

    Boolean sortingDirectionIsDown = true;
    Boolean isSortedAlphabetically = true;

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

        //makeStringRequest(requestQueue, "http://www.fewgamers.com/api.php");

        String jsonString = "[{\"game\":\"CoD 2\",\"serverName\":\"Server One\",\"playercount\":\"8/10\",\"ip\":\"192.168.62.3\",\"creator\":\"jack\"},{\"game\":\"DoD\", \"serverName\":\"Server Two\",\"playercount\":\"9/22\",\"ip\":\"192.168.2.1\",\"creator\":\"luuk\"},{\"game\":\"SC:BW\", \"serverName\":\"Server Three\",\"playercount\":\"2/4\",\"ip\":\"190.68.1.0\",\"creator\":\"jack\"}]";

        Map<String, ArrayList<ServerObject>> m = makeServerList(jsonString);

        serverListAlphabetical = m.get("Alphabetical");
        serverListByPlayers = m.get("Numerical");
        serverList = new ArrayList<ServerObject>(m.get("Alphabetical"));

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
                } else {
                    sortingButton.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                    sortingDirectionIsDown = true;
                }
                Collections.reverse(serverList);
                serverAdapter.notifyDataSetChanged();
            }
        });

        sortingButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pickSortingMethod();

                return true;
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        FragServerInfo fragment = new FragServerInfo();
        fragment.setServer((ServerObject) l.getItemAtPosition(position));

        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            ft.commit();
        }
    }

    private void searchFilter(String search) {
        ArrayList<ServerObject> referenceList;
        if (isSortedAlphabetically) {
            referenceList = new ArrayList<ServerObject>(serverListAlphabetical);
        } else {
            referenceList = new ArrayList<ServerObject>(serverListByPlayers);
        }

        serverList.clear();

        for (ServerObject so : referenceList) {
            if (so.getServerName().toLowerCase().contains(search)) {
                serverList.add(so);
            }
        }
        serverAdapter.notifyDataSetChanged();
    }

    private void pickSortingMethod() {
        AlertDialog.Builder sortingBuilder = new AlertDialog.Builder(getActivity());
        sortingBuilder.setTitle("Sort by ...")
                .setPositiveButton("Server name", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setAlphabetical();
                    }

                })
                .setNegativeButton("Live players", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setNumerical();
                    }
                });
        AlertDialog alertDialog = sortingBuilder.create();
        alertDialog.show();
    }

    private void setAlphabetical() {
        isSortedAlphabetically = true;
        searchFilter(searchBar.getText().toString().toLowerCase());
    }

    private void setNumerical() {
        isSortedAlphabetically = false;
        searchFilter(searchBar.getText().toString().toLowerCase());
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

    private Map<String, ArrayList<ServerObject>> makeServerList(String jsonString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Server list not found", "Something went wrong when loading server data");
        }

        ArrayList<ServerObject> resAlphabetical = new ArrayList<>();
        ArrayList<ServerObject> resNumerical = new ArrayList<>();

        Integer successCounter = 0;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                ServerObject serverObject = new ServerObject();
                serverObject.defineServer(jsonArray.getJSONObject(i));

                Integer j = successCounter - 1;

                while (j > - 1 && resAlphabetical.get(j).getServerName().toLowerCase().compareTo(serverObject.getServerName().toLowerCase()) > 0) {
                    j--;
                }
                resAlphabetical.add(j + 1, serverObject);

                j = successCounter - 1;

                while (j > -1 && resNumerical.get(j).getLivePlayer() < serverObject.getLivePlayer()) {
                    j--;
                }
                resNumerical.add(j + 1, serverObject);

                successCounter++;
            } catch (JSONException exception) {
                Log.e("Server object missing", "Server object data incomplete");
            }
        }

        Map<String, ArrayList<ServerObject>> map = new HashMap<>();
        map.put("Alphabetical", resAlphabetical);
        map.put("Numerical", resNumerical);
        return map;
    }


}
