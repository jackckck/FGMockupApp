package com.fewgamers.fewgamers;

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
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragServerBrowser extends ListFragBase {
    // the contents of serverList mirror the content on the user's screen
    // serverListAlphabetical and serverListByPlayers both contain all registered servers that match the
    // user's search filters
    ArrayList<ServerObject> serverList, serverListAlphabetical, serverListByPlayers;

    EditText searchBar;
    ImageButton sortingButton, advancedFiltersButton;
    FloatingActionButton refreshFAB;
    TextView failedToLoadServersText;

    ServerListAdapter serverAdapter;

    Boolean sortingDirectionIsDown = true;
    Boolean isSortedAlphabetically = true;

    Integer[] maxPlayerLimits;
    String[] allowedGameUUIDs;

    boolean gamesFilterActive;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragserverbrowser, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        // loading servers begins
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        serverList = new ArrayList<>();
        serverListAlphabetical = new ArrayList<>();
        serverListByPlayers = new ArrayList<>();
        serverAdapter = new ServerListAdapter(getActivity(), serverList);
        setListAdapter(serverAdapter);

        // retrieving widgets
        searchBar = (EditText) mainActivity.findViewById(R.id.serverSearchBar);
        sortingButton = (ImageButton) getActivity().findViewById(R.id.sortingButton);
        advancedFiltersButton = (ImageButton) mainActivity.findViewById(R.id.serverFilterButton);
        // a textview at the screen's center to indicate errors
        failedToLoadServersText = (TextView) mainActivity.findViewById(R.id.serverBrowserTextView);
        refreshFAB = (FloatingActionButton) mainActivity.findViewById(R.id.serverBrowserRefreshFAB);


        getFilterPreferences();

        // checks for existing serverlist stored in the MainActivity
        if (mainActivity.hasServerListStored) {
            extractServerListsFromJSONArray(mainActivity.completeServerListString);
        } else {
            refreshServerList();
        }

        // restores the searchbar's previous text value
        searchBar.setText(mainActivity.serverSearchBarText);

        // calls the server browser's filter method after each change in the searchbar's text
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

        // changes the sorting direction, indicated by an upward or downward arrow
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

        // allows sorting by server name and by maximum playercount
        sortingButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pickSortingMethod();
                return true;
            }
        });

        // opens an instance of FragServerBrowserFilter
        advancedFiltersButton.setOnClickListener(new View.OnClickListener() {
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

        // clicking the fab refreshes the server list
        refreshFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshServerList();
            }
        });
    }

    // clicking a server opens a fragment that displays additional information about it
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        openServerInfo((ServerObject) l.getItemAtPosition(position), mainActivity);
    }

    // opens a fragment that displays additional information about a given ServerObject
    public static void openServerInfo(ServerObject serverObject, MainActivity mainActivity) {
        FragServerInfo fragment = new FragServerInfo();
        fragment.setServer(serverObject);


        if (fragment != null) {
            mainActivity.onBackServerObjects.add(0, serverObject);
            mainActivity.previousFragId.add(0, R.integer.display_fragment_server_info_id);

            FragmentTransaction ft = mainActivity.getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            ft.commit();
        }
    }

    // filters out any server whose name does not match
    private void searchFilter(String search) {
        // takes the complete list of servers, and adds any server that matches the search query
        // back to the currently displayed servers
        ArrayList<ServerObject> referenceList;
        if (isSortedAlphabetically) {
            referenceList = new ArrayList<>(serverListAlphabetical);
        } else {
            referenceList = new ArrayList<>(serverListByPlayers);
        }

        serverList.clear();

        for (ServerObject so : referenceList) {
            if (so.getServerName().toLowerCase().contains(search)) {
                serverList.add(so);
            }
        }

        updateServerBrowser();
    }

    // shows a dialog that allows a user to choose their sorting method
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

    // retrieves a JSONArray containing all registered servers, and calls method to extract a server
    // list from them
    private void requestServerList(RequestQueue queue, String url) {
        serverAdapter.clear();
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);
        failedToLoadServersText.setText("");
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mainActivity.completeServerListString = response;

                if (response.equals("{'error': 'unauthorised api key'}")) {
                    failedToLoadServersText.setText("Unauthorised user");
                    mainActivity.mainProgressBar.setVisibility(View.GONE);
                    return;
                }

                mainActivity.hasServerListStored = true;

                extractServerListsFromJSONArray(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainActivity.mainProgressBar.setVisibility(View.GONE);
                failedToLoadServersText.setText("Could not retrieve servers");
                Log.e("Server error", error.toString());
            }
        });

        queue.add(stringRequest);
    }

    // creates the server ArrayLists, and displays them onscreen
    private void extractServerListsFromJSONArray(String jsonString) {
        Map<String, ArrayList<ServerObject>> serverListMap = makeServerList(jsonString);

        if (serverListMap == null) {
            failedToLoadServersText.setText("Something went wrong with the retrieved server data");
            return;
        }
        serverListAlphabetical = serverListMap.get("Alphabetical");
        serverListByPlayers = serverListMap.get("Numerical");
        serverList.clear();

        // sorting is alphabetical by default
        serverList.addAll(serverListAlphabetical);
        
        String search = mainActivity.serverSearchBarText;
        if (search != null) {
            searchFilter(search);
        }

        // all server lists are ready
        mainActivity.mainProgressBar.setVisibility(View.GONE);

        updateServerBrowser();
    }

    // updates the servers displayed onscreen to reflect serverList
    private void updateServerBrowser() {
        serverAdapter.notifyDataSetChanged();

        if (serverList.size() == 0) {
            failedToLoadServersText.setText("No servers to match filter");
        } else {
            failedToLoadServersText.setText("");
        }
    }

    // method that returns two complete lists of servers. one is sorted alphabetically, and the other
    // by the maximum playercount
    private Map<String, ArrayList<ServerObject>> makeServerList(String jsonString) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Server list not found", "Something went wrong when loading server data");
            exception.printStackTrace();
            return null;
        }

        ArrayList<ServerObject> resAlphabetical = new ArrayList<>();
        ArrayList<ServerObject> resByPlayer = new ArrayList<>();

        // Integer that goes up by one every time a server passes through the current filter
        Integer addedCounter = 0;
        // a loop that goes by every server in the retrieved JSONArray, and turns it into a
        // ServerObject if it passes through filter, it is added to both serverListAlphabetical and
        // serverListByPlayers in its correct position
        for (int i = 0; i < jsonArray.length(); i++) {
            ServerObject serverObject = new ServerObject();
            try {
                serverObject.defineServer(jsonArray.getJSONObject(i));
            } catch (JSONException exception) {
                Log.e("Server data missing", "Some property of the server object could not be found inside the JSON string.");
                exception.printStackTrace();
            }

            Integer maxPlayer;
            maxPlayer = serverObject.getMaxPlayer();
            String game = serverObject.getGameUUID();

            // a server is only considered if its max playercount is within bounds, and its game
            // is not excluded by the games filter
            if (passesFilter(maxPlayer, game)) {
                // index of the last element of serverListAlphabetical
                Integer j = addedCounter - 1;

                // loop that starts at the alphabetical list's last element, and moves downward until
                // the first element is found whose name comes before the name of the not yet added
                // ServerObject. that ServerObject is then added just behind the found element
                while (j > -1 && resAlphabetical.get(j).getServerName().toLowerCase().compareTo(serverObject.getServerName().toLowerCase()) > 0) {
                    j--;
                }
                resAlphabetical.add(j + 1, serverObject);

                // index of the last element of serverListByPlayers
                j = addedCounter - 1;

                // loops downward from serverListByPlayers's last element until the first element is
                // found whose max playercount is lower than that of the not yet added ServerObject
                // that ServerObject is then added just behind the found element
                while (j > -1 && resByPlayer.get(j).getMaxPlayer() < serverObject.getMaxPlayer()) {
                    j--;
                }
                resByPlayer.add(j + 1, serverObject);

                addedCounter++;
            }
        }

        Map<String, ArrayList<ServerObject>> map = new HashMap<>();
        map.put("Alphabetical", resAlphabetical);
        map.put("Numerical", resByPlayer);
        return map;
    }

    // sets FragServerBrowser's server filter fields, using the values set in the application's
    // settings screen
    private void getFilterPreferences() {
        maxPlayerLimits = new Integer[2];

        // preferences set in the app's settings screen
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String[] strings = defaultPreferences.getString(getResources().getString(R.string.pref_servers_filter_subscreen_playercap_key), "null-null").split("-");

        if (strings[0].equals("null")) {
            maxPlayerLimits[0] = -1;
        } else {
            maxPlayerLimits[0] = Integer.parseInt(strings[0]);
        }
        if (strings[1].equals("null")) {
            maxPlayerLimits[1] = 9999;
        } else {
            maxPlayerLimits[1] = Integer.parseInt(strings[1]);
        }

        allowedGameUUIDs = defaultPreferences.getString(getResources().getString(R.string.pref_servers_filter_subscreen_game_filter_key), "").split(",,");

        gamesFilterActive = (allowedGameUUIDs.length > 1);
    }

    // returns true of the given values are within the filter's bounds
    private boolean passesFilter(Integer maxPlayer, String gameUUID) {
        if (maxPlayer < maxPlayerLimits[0] || maxPlayerLimits[1] < maxPlayer) {
            return false;
        }
        if (gamesFilterActive && !Arrays.asList(allowedGameUUIDs).contains(gameUUID)) {
            return false;
        }
        return true;
    }

    // override that saves the text value in the search bar
    @Override
    public void onStop() {
        super.onStop();
        mainActivity.serverSearchBarText = searchBar.getText().toString();
    }

    // retrieves fresh server data
    private void refreshServerList() {
        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
        requestServerList(requestQueue, "https://www.fewgamers.com/api/server/?key=" + mainActivity.getMyKey());
    }
}
