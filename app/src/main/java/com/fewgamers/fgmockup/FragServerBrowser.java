package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    Integer[] maxPlayerLimits;
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

    private void pickSortingMethod() {
        AlertDialog.Builder sortingBuilder = new AlertDialog.Builder(getActivity());
        sortingBuilder.setTitle("Sort by ...")
                .setPositiveButton("Server name", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setAlphabetical();
                    }

                })
                .setNegativeButton("Max players", new DialogInterface.OnClickListener() {
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

            Integer maxPlayer;
            maxPlayer = serverObject.getMaxPlayer();
            String game = serverObject.getGameUUID();

            if (passesFilter(maxPlayer, game)) {
                Integer j = successCounter - 1;

                while (j > -1 && resAlphabetical.get(j).getServerName().toLowerCase().compareTo(serverObject.getServerName().toLowerCase()) > 0) {
                    j--;
                }
                resAlphabetical.add(j + 1, serverObject);

                j = successCounter - 1;

                while (j > -1 && resNumerical.get(j).getMaxPlayer() < serverObject.getMaxPlayer()) {
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
        maxPlayerLimits = new Integer[2];

        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        String[] strings = defaultPreferences.getString(getResources().getString(R.string.pref_servers_filter_subscreen_playercap_key), "null-null").split("-");

        if (strings[0].equals("null")) {
            maxPlayerLimits[0] = -1;
        } else {
            maxPlayerLimits[0] = Integer.parseInt(strings[0]);
        }
        if (strings[1].equals("null")) {
            maxPlayerLimits[1] = 1000;
        } else {
            maxPlayerLimits[1] = Integer.parseInt(strings[1]);
        }

        hideFull = defaultPreferences.getBoolean(getResources().getString(R.string.pref_servers_filter_subscreen_hide_full_key), false);

        allowedGames = defaultPreferences.getString(getResources().getString(R.string.pref_servers_filter_subscreen_game_filter_key), "").split(",,");
        gamesFilterActive = (allowedGames.length > 0);
    }

    private boolean passesFilter(Integer maxPlayer, String game) {
        if (maxPlayer < maxPlayerLimits[0] || maxPlayerLimits[1] < maxPlayer) {
            return false;
        }
        return true;
    }
}
