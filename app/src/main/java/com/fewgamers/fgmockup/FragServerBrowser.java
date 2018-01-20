package com.fewgamers.fgmockup;

import android.app.Activity;
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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ProgressBar;
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
import java.util.prefs.Preferences;

/**
 * Created by Administrator on 12/6/2017.
 */

// hier komt de server browser. momenteel maakt hij al contact met onze server, maar hij levert nog niets zinnigs op
public class FragServerBrowser extends ListFragBase {
    ArrayList<ServerObject> serverList, serverListAlphabetical, serverListByPlayers;
    FloatingActionButton fab;

    ServerListAdapter serverAdapter;

    EditText searchBar;
    ImageButton sortingButton, filterButton;

    Boolean sortingDirectionIsDown = true;
    Boolean isSortedAlphabetically = true;

    String serverListString;

    Integer[] playerLimits;
    String[] allowedGames;

    Boolean hideEmpty, hideFull;

    boolean notReady, gamesFilterActive;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragserverbrowser, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // hier wordt een nieuwe requestview geïnstantieerd als die nog niet bestaat. als hij wel bestaat wordt de bestaande
        // requestview opgehaald.
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        //begint het laden van de serverlist
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        getFilterPreferences();

        //checkt of er al een serverlist gedownload is.
        if (mainActivity.hasServerListStored) {
            extractServerListFromJSONString(mainActivity.completeServerListString);
        } else {
            requestServerList(requestQueue, "https://www.fewgamers.com/api/server/?key=" + mainActivity.key);
        }

        // alles hieronder is voor buttons en edittexts
        searchBar = (EditText) mainActivity.findViewById(R.id.serverSearchBar);
        filterButton = (ImageButton) mainActivity.findViewById(R.id.serverFilterButton);

        searchBar.setText(mainActivity.serverSearchFilter);

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
                serverAdapter.notifyDataSetChanged();
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

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragServerBrowserFilter fragment = new FragServerBrowserFilter();

                if (fragment != null) {
                    mainActivity.mainProgressBar.setVisibility(View.GONE);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.MyFrameLayout, fragment);
                    ft.commit();
                }
            }
        });

        fab = (FloatingActionButton) mainActivity.findViewById(R.id.serverBrowserRefreshFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverAdapter.clear();
                mainActivity.mainProgressBar.setVisibility(View.VISIBLE);
                requestServerList(requestQueue, "https://www.fewgamers.com/api/server/?key=" + mainActivity.key);
            }
        });
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
        if (notReady) {
            return;
        }
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

        mainActivity.serverSearchFilter = search;
    }

    private void playerCountFilter() {
        Integer[] playerLimit = mainActivity.playerCountLimit;
        Integer playerMin, playerMax, maxPlayerMin, maxPlayerMax, livePlayer, maxPlayer;
        playerMin = playerLimit[0];
        playerMax = playerLimit[1];
        maxPlayerMin = playerLimit[2];
        maxPlayerMax = playerLimit[3];

        for (int i = 0; i < serverListAlphabetical.size(); i++) {
            ServerObject so = serverListAlphabetical.get(i);
            livePlayer = so.getLivePlayer();
            if ((playerMin != null && livePlayer < playerMin) || (playerMax != null && playerMax < livePlayer)) {
                serverListAlphabetical.remove(i);
                serverListByPlayers.remove(so);
                break;
            }
            maxPlayer = so.getMaxPlayer();
            if ((maxPlayerMin != null && maxPlayer < maxPlayerMin) || (maxPlayerMax != null && maxPlayerMax < maxPlayer)) {
                serverListAlphabetical.remove(i);
                serverListByPlayers.remove(so);
            }
        }
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

    private void requestServerList(RequestQueue queue, String url) {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mainActivity.completeServerListString = response;
                mainActivity.hasServerListStored = true;

                Log.d("Server list", response);
                extractServerListFromJSONString(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Server error", error.toString());
            }
        });

        queue.add(stringRequest);
    }

    private void extractServerListFromJSONString(String jsonString) {
        Map<String, ArrayList<ServerObject>> serverListMap = makeServerList(jsonString);

        serverListAlphabetical = serverListMap.get("Alphabetical");
        serverListByPlayers = serverListMap.get("Numerical");
        serverList = new ArrayList<>(serverListMap.get("Alphabetical"));

        String search = mainActivity.serverSearchFilter;
        if (search != null) {
            searchFilter(search);
        }

        serverAdapter = new ServerListAdapter(getActivity(), serverList);
        setListAdapter(serverAdapter);

        // laden is voorbij
        mainActivity.mainProgressBar.setVisibility(View.GONE);
        notReady = false;
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
            ServerObject serverObject = new ServerObject();

            try {
                serverObject.defineServer(jsonArray.getJSONObject(i));
            } catch (JSONException exception) {
                Log.e("Server data missing", "Some property of the server object could not be found inside the JSON string.");
            }

            Integer playerCount, playerCap;
            playerCount = serverObject.getLivePlayer();
            playerCap = serverObject.getMaxPlayer();
            String game = serverObject.getGame();

            if (passesFilter(playerCount, playerCap, game)) {
                Integer j = successCounter - 1;

                while (j > -1 && resAlphabetical.get(j).getServerName().toLowerCase().compareTo(serverObject.getServerName().toLowerCase()) > 0) {
                    j--;
                }
                resAlphabetical.add(j + 1, serverObject);

                j = successCounter - 1;

                while (j > -1 && resNumerical.get(j).getLivePlayer() < serverObject.getLivePlayer()) {
                    j--;
                }
                resNumerical.add(j + 1, serverObject);

                successCounter++;
            }
        }

        Map<String, ArrayList<ServerObject>> map = new HashMap<>();
        map.put("Alphabetical", resAlphabetical);
        map.put("Numerical", resNumerical);
        return map;
    }

    private void getFilterPreferences() {
        playerLimits = new Integer[4];
        String[] strings0, strings1;

        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        strings0 = defaultPreferences.getString(getResources().getString(R.string.pref_servers_filter_subscreen_playercount_key), "null-null").split("-");
        strings1 = defaultPreferences.getString(getResources().getString(R.string.pref_servers_filter_subscreen_playercap_key), "null-null").split("-");

        if (strings0[0].equals("null")) {
            playerLimits[0] = -1;
        } else {
            playerLimits[0] = Integer.parseInt(strings0[0]);
        }
        if (strings0[1].equals("null")) {
            playerLimits[1] = 1000;
        } else {
            playerLimits[1] = Integer.parseInt(strings0[1]);
        }
        if (strings1[0].equals("null")) {
            playerLimits[2] = -1;
        } else {
            playerLimits[2] = Integer.parseInt(strings1[0]);
        }
        if (strings1[1].equals("null")) {
            playerLimits[3] = 1000;
        } else {
            playerLimits[3] = Integer.parseInt(strings0[1]);
        }

        hideEmpty = defaultPreferences.getBoolean(getResources().getString(R.string.pref_servers_filter_subscreen_hide_empty_key), false);
        hideFull = defaultPreferences.getBoolean(getResources().getString(R.string.pref_servers_filter_subscreen_hide_full_key), false);

        allowedGames = defaultPreferences.getString(getResources().getString(R.string.pref_servers_filter_subscreen_game_filter_key), "").split(",,");
        gamesFilterActive = (allowedGames.length > 0);
    }

    private boolean passesFilter(Integer playerCount, Integer playerCap, String game) {
        if (hideEmpty && playerCount.equals(0)) {
            return false;
        }
        if (hideFull && playerCount.equals(playerCap)) {
            return false;
        }
        if (playerCount < playerLimits[0] || playerLimits[1] < playerCount || playerCap < playerLimits[2] || playerLimits[3] < playerCap) {
            return false;
        }

        return true;
    }
}
