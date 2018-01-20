package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
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
import java.util.Arrays;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragUser extends ListFragBase {

    String myServersString, favServersString;
    ArrayList<ServerObject> userServerList, myServersList, favServersList;

    String uuid = "2532ef98f6034792baf22471073683f6";

    ServerListAdapter userAdapter;

    TabLayout.OnTabSelectedListener userTabsSelect;

    TextView usernameText, emailText, firstNameText, lastNameText, usernameDisplay, emailDisplay, firstNameDisplay, lastNameDisplay;

    private boolean notReady = true;

    FloatingActionButton userFAB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraguserinfo, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        // laden begint
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        // widgets ophalen
        usernameDisplay = mainActivity.findViewById(R.id.friendsInfoUsernameDisplay);
        emailDisplay = mainActivity.findViewById(R.id.friendsInfoEMailDisplay);
        firstNameDisplay = mainActivity.findViewById(R.id.friendsInfoFirstNameDisplay);
        lastNameDisplay = mainActivity.findViewById(R.id.friendsInfoLastNameDisplay);

        usernameText = mainActivity.findViewById(R.id.friendsInfoUsername);
        emailText = mainActivity.findViewById(R.id.friendsInfoEMail);
        firstNameText = mainActivity.findViewById(R.id.friendsInfoFirstName);
        lastNameText = mainActivity.findViewById(R.id.friendsInfoLastName);

        userFAB = mainActivity.findViewById(R.id.addServerFAB);

        setUserDisplays();

        favServersList = new ArrayList<>();

        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // requestMyServers(requestQueue, mainActivity.uuid);
        requestMyServers(requestQueue, uuid);

        switch (mainActivity.userTabSelected) {
            case 0:
                toggleDisplaysVisibility(true);
                userFAB.setVisibility(View.GONE);
                break;
            case 1:
                toggleDisplaysVisibility(false);
                userFAB.setVisibility(View.VISIBLE);
                break;
            case 2:
                toggleDisplaysVisibility(false);
                userFAB.setVisibility(View.GONE);
                break;
        }

        userTabsSelect = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (notReady) {
                    toggleDisplaysVisibility(false);
                    return;
                }
                userServerList.clear();
                switch (tab.getPosition()) {
                    case 0:
                        mainActivity.userTabSelected = 0;
                        toggleDisplaysVisibility(true);
                        userFAB.setVisibility(View.GONE);
                        break;
                    case 1:
                        mainActivity.userTabSelected = 1;
                        userServerList.addAll(myServersList);
                        toggleDisplaysVisibility(false);
                        userFAB.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        mainActivity.userTabSelected = 2;
                        userServerList.addAll(favServersList);
                        toggleDisplaysVisibility(false);
                        userFAB.setVisibility(View.GONE);
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

        userFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View addServerDialogView = inflater.inflate(R.layout.dialog_add_server, null);
                AlertDialog.Builder addServerDialogBuilder = new AlertDialog.Builder(getActivity());
                addServerDialogBuilder.setView(addServerDialogView);

                final MultiAutoCompleteTextView addServerAutoComplete = addServerDialogView.findViewById(R.id.addServerGameAutoComplete);
                addServerAutoComplete.setAdapter(new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, mainActivity.getAllGames()));
                addServerAutoComplete.setThreshold(1);
                addServerAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                addServerAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedGame = (String) parent.getItemAtPosition(position);
                        addServerAutoComplete.setText(selectedGame);
                    }
                });

                addServerDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        View addServerConfirmDialogView = inflater.inflate(R.layout.dialog_confirm_add_server, null);
                        String name = ((EditText) addServerDialogView.findViewById(R.id.addServerNameEdit)).getText().toString();
                        String game = addServerAutoComplete.getText().toString();
                        String ip = ((EditText) addServerDialogView.findViewById(R.id.addServerIPEdit)).getText().toString();
                        String playerCap = ((EditText) addServerDialogView.findViewById(R.id.addServerPlayerCapEdit)).getText().toString();

                        showAddServerConfirmDialog(addServerConfirmDialogView, name, game, ip, playerCap);
                    }
                });
                AlertDialog addServerDialog = addServerDialogBuilder.create();
                addServerDialog.show();
                addServerDialog.getWindow().setLayout(585, 820);
            }
        });
    }

    private void showAddServerConfirmDialog(final View dialogView, final String name, final String game, final String ip, final String playerCap) {
        if (name.equals("") || game.equals("") || ip.equals("")) {
            Toast.makeText(getActivity(), "Required field missing", Toast.LENGTH_SHORT).show();
            return;
        }
        int indexOf = Arrays.asList(mainActivity.getAllGames()).indexOf(game);
        String gameUUID;
        if (indexOf == -1) {
            Toast.makeText(getActivity(), "Invalid game", Toast.LENGTH_SHORT).show();
            return;
        } else {
            gameUUID = mainActivity.getAllGamesUUIDs()[indexOf];
        }
        AlertDialog.Builder addServerConfirmDialogBuilder = new AlertDialog.Builder(getActivity());
        addServerConfirmDialogBuilder.setView(dialogView);
        addServerConfirmDialogBuilder.setTitle("Add the following server?");
        ((TextView) dialogView.findViewById(R.id.confirmAddServerName)).setText(name);
        ((TextView) dialogView.findViewById(R.id.confirmAddServerGame)).setText(gameUUID);
        ((TextView) dialogView.findViewById(R.id.confirmAddServerIP)).setText(ip);
        ((TextView) dialogView.findViewById(R.id.confirmAddServerPlayerCount)).setText(playerCap);
        addServerConfirmDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String additionalData = ((EditText) dialogView.findViewById(R.id.confirmAddServerAdditionalDataEdit)).getText().toString();
                addServer(name, game, ip, playerCap, additionalData);
            }
        });
        addServerConfirmDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog addServerConfirmDialog = addServerConfirmDialogBuilder.create();
        addServerConfirmDialog.show();
    }

    private void addServer(String name, String game, String ip, String playerCap, String additionalData) {
        JSONObject userdata = new JSONObject();
        try {
            userdata.put("name", name);
            userdata.put("game", "67f105531a0041479ee91de939a1f192");
            userdata.put("ip", ip);
            userdata.put("creator", mainActivity.uuid);
            if (playerCap.equals("")) {
                userdata.put("playercount", "None");
            } else {
                userdata.put("playercount", playerCap);
            }
            userdata.put("additionaldata", additionalData);
        } catch (JSONException exception) {
            Log.e("addServer JSONException", "Something went wrong when constructing addServer userdata JSONObject");
        }
        JSONObject finalQuery = new JSONObject();
        FGEncrypt fgEncrypt = new FGEncrypt();
        try {
            finalQuery.put("key", mainActivity.master);
            finalQuery.put("userdata", fgEncrypt.encrypt(userdata.toString()));
        } catch (JSONException exception) {
            Log.e("addServer JSONException", "Something went wrong when constructing addServer finalQuery JSONObject");
        }
        Log.d("userdata unencrypted", userdata.toString());
        Log.d("addserver final", finalQuery.toString());
        new FGAsyncTask().execute("https://fewgamers.com/api/server/", finalQuery.toString(), "POST");
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
                notReady = false;
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
