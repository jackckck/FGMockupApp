package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
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

    ArrayList<ServerObject> myServersList, currentServersList;

    ServerListAdapter userAdapter;

    TextView usernameText, emailText, firstNameText, lastNameText, usernameDisplay,
            emailDisplay, firstNameDisplay, lastNameDisplay, noServersFoundText;

    private boolean notReady = true;

    FloatingActionButton userFAB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraguser, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        // laden begint
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        // adapter instellen
        myServersList = new ArrayList<>();
        currentServersList = new ArrayList<>();
        userAdapter = new ServerListAdapter(getActivity(), currentServersList);
        setListAdapter(userAdapter);

        // eigen servers ophalen
        requestMyServers();

        // widgets ophalen
        usernameDisplay = mainActivity.findViewById(R.id.friendsInfoUsernameDisplay);
        emailDisplay = mainActivity.findViewById(R.id.friendsInfoEMailDisplay);
        firstNameDisplay = mainActivity.findViewById(R.id.friendsInfoFirstNameDisplay);
        lastNameDisplay = mainActivity.findViewById(R.id.friendsInfoLastNameDisplay);

        usernameText = mainActivity.findViewById(R.id.friendsInfoUsername);
        emailText = mainActivity.findViewById(R.id.friendsInfoEMail);
        firstNameText = mainActivity.findViewById(R.id.friendsInfoFirstName);
        lastNameText = mainActivity.findViewById(R.id.friendsInfoLastName);

        noServersFoundText = mainActivity.findViewById(R.id.userInfoNoServersFoundText);

        userFAB = mainActivity.findViewById(R.id.addServerFAB);

        setUserDisplays();

        //mainActivity.friendsTabs.getTabAt(mainActivity.userTabSelected).select();
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

        mainActivity.friendsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (notReady) {
                    toggleDisplaysVisibility(false);
                    return;
                }
                currentServersList.clear();
                switch (tab.getPosition()) {
                    case 0:
                        mainActivity.userTabSelected = 0;
                        toggleDisplaysVisibility(true);
                        userFAB.setVisibility(View.GONE);
                        noServersFoundText.setVisibility(View.GONE);
                        break;
                    case 1:
                        mainActivity.userTabSelected = 1;
                        toggleDisplaysVisibility(false);
                        currentServersList.addAll(myServersList);
                        userFAB.setVisibility(View.VISIBLE);
                        noServersFoundText.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        mainActivity.userTabSelected = 2;
                        toggleDisplaysVisibility(false);
                        userFAB.setVisibility(View.GONE);
                        noServersFoundText.setVisibility(View.GONE);
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

        userFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View addServerDialogView = inflater.inflate(R.layout.dialog_add_server, null);
                AlertDialog.Builder addServerDialogBuilder = new AlertDialog.Builder(getActivity());
                addServerDialogBuilder.setView(addServerDialogView);

                final MultiAutoCompleteTextView addServerAutoComplete = addServerDialogView.findViewById(R.id.add_server_autocomplete);
                addServerAutoComplete.setAdapter(new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, mainActivity.getAllGamesArray()));
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
                        String name = ((EditText) addServerDialogView.findViewById(R.id.add_server_name)).getText().toString();
                        String game = addServerAutoComplete.getText().toString();
                        String ip = ((EditText) addServerDialogView.findViewById(R.id.add_server_ip)).getText().toString();
                        String playerCap = ((EditText) addServerDialogView.findViewById(R.id.add_server_playercap)).getText().toString();

                        if (name.equals("") || game.equals("") || ip.equals("")) {
                            Toast.makeText(getActivity(), "Required field missing", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        showAddServerConfirmDialog(addServerConfirmDialogView, name, game, ip, playerCap);
                    }
                });
                AlertDialog addServerDialog = addServerDialogBuilder.create();
                addServerDialog.show();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] clickMyServerOptions = new String[2];

        ServerObject clickedServer = (ServerObject) l.getItemAtPosition(position);
        final String clickedServerUUID, clickedServerName, clickedServerGame, clickedServerIP, clickedServerPlayercap, clickedServerAdditionalInfo;
        clickedServerUUID = clickedServer.getServerUUID();
        clickedServerName = clickedServer.getServerName();
        clickedServerGame = mainActivity.getGameNameFromUUID(clickedServer.getGameUUID());
        clickedServerIP = clickedServer.getIp();
        clickedServerPlayercap = clickedServer.getPlayerCap();
        clickedServerAdditionalInfo = clickedServer.getAdditionalInfo();

        clickMyServerOptions[0] = "Edit";
        clickMyServerOptions[1] = "Remove " + clickedServerName;

        builder.setItems(clickMyServerOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showEditServerDialog(clickedServerUUID, clickedServerName, clickedServerGame, clickedServerPlayercap, clickedServerAdditionalInfo);
                        break;
                    case 1:
                        AlertDialog.Builder removeServerConfirmDialog = new AlertDialog.Builder(getActivity());
                        removeServerConfirmDialog.setTitle("Remove " + clickedServerName + "?");
                        removeServerConfirmDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeServer(clickedServerUUID);
                            }
                        });
                        removeServerConfirmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        removeServerConfirmDialog.show();
                        break;
                }
            }
        });
        builder.show();
    }

    private void showEditServerDialog(final String uuid, String serverName, String serverGame, String serverPlayercap, String serverAdditionalInfo) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View editServerDialogView = inflater.inflate(R.layout.dialog_edit_server, null);
        AlertDialog.Builder editServerDialogBuilder = new AlertDialog.Builder(getActivity());
        editServerDialogBuilder.setView(editServerDialogView);

        // get widgets
        final EditText editServerName = (EditText) editServerDialogView.findViewById(R.id.edit_server_name);
        final MultiAutoCompleteTextView editServerGameAutoComplete = editServerDialogView.findViewById(R.id.edit_server_autocomplete);
        final EditText editServerPlayercap = (EditText) editServerDialogView.findViewById(R.id.edit_server_playercap);
        final EditText editServerAdditionalInfo = (EditText) editServerDialogView.findViewById(R.id.edit_server_additional_info);

        editServerName.setText(serverName);
        editServerGameAutoComplete.setText(serverGame);
        editServerPlayercap.setText(serverPlayercap);
        editServerAdditionalInfo.setText(serverAdditionalInfo);

        editServerGameAutoComplete.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, mainActivity.getAllGamesArray()));
        editServerGameAutoComplete.setThreshold(1);
        editServerGameAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        editServerGameAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedGame = (String) parent.getItemAtPosition(position);
                editServerGameAutoComplete.setText(selectedGame);
            }
        });

        editServerDialogBuilder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editServerName.getText().toString();
                String game = editServerGameAutoComplete.getText().toString();
                String gameUUID;
                try {
                    gameUUID = mainActivity.getGameUUIDFromName(game);
                } catch (ArrayIndexOutOfBoundsException exception) {
                    Toast.makeText(getActivity(), "Not a valid game", Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                    return;
                }
                String playerCap = editServerPlayercap.getText().toString();
                String additionalInfo = editServerAdditionalInfo.getText().toString();

                editMyServer(uuid, name, gameUUID, playerCap, additionalInfo);
            }
        });

        editServerDialogBuilder.show();
    }

    private void editMyServer(String uuid, String name, String gameUUID, String playercap, String additionalInfo) {
        JSONObject userDataJSONObject = new JSONObject();
        try {
            userDataJSONObject.put("uuid", uuid);
            userDataJSONObject.put("name", name);
            userDataJSONObject.put("game", gameUUID);
            userDataJSONObject.put("playercount", playercap);
            userDataJSONObject.put("additionaldata", additionalInfo);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        FGEncrypt fgEncrypt = new FGEncrypt();
        String encryptedUserdata = fgEncrypt.encrypt(userDataJSONObject.toString());
        String finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, mainActivity.master);

        new ChangeMyServersListAsyncTask().execute("https://fewgamers.com/api/server/", finalQuery, "PATCH");
    }

    private void removeServer(String serverUUID) {
        JSONObject finalQueryJSONObject = new JSONObject();
        try {
            finalQueryJSONObject.put("key", mainActivity.master);
            finalQueryJSONObject.put("uuid", serverUUID);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        Log.d("finalQuery", finalQueryJSONObject.toString());
        new ChangeMyServersListAsyncTask().execute("https://fewgamers.com/api/server/", finalQueryJSONObject.toString(), "DELETE");
    }

    private class ChangeMyServersListAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            if (response[0].equals("404")) {
                Toast.makeText(getActivity(), "Server not found in database", Toast.LENGTH_SHORT).show();
            }
            requestMyServers();
        }
    }

    private void showAddServerConfirmDialog(final View dialogView, final String name, final String game, final String ip, final String playerCap) {
        if (name.equals("") || game.equals("") || ip.equals("")) {
            Toast.makeText(getActivity(), "Required field missing", Toast.LENGTH_SHORT).show();
            return;
        }
        int indexOf = Arrays.asList(mainActivity.getAllGamesArray()).indexOf(game);
        String gameUUID;
        if (indexOf == -1) {
            Toast.makeText(getActivity(), "Invalid game", Toast.LENGTH_SHORT).show();
            return;
        } else {
            gameUUID = mainActivity.getAllGamesUUIDsArray()[indexOf];
        }
        AlertDialog.Builder addServerConfirmDialogBuilder = new AlertDialog.Builder(getActivity());
        addServerConfirmDialogBuilder.setView(dialogView);
        addServerConfirmDialogBuilder.setTitle("Add the following server?");
        ((TextView) dialogView.findViewById(R.id.confirmAddServerName)).setText(name);
        ((TextView) dialogView.findViewById(R.id.confirmAddServerGame)).setText(game);
        ((TextView) dialogView.findViewById(R.id.confirmAddServerIP)).setText(ip);
        ((TextView) dialogView.findViewById(R.id.confirmAddServerPlayerCount)).setText(playerCap);
        addServerConfirmDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String additionalData = ((EditText) dialogView.findViewById(R.id.confirmAddServerAdditionalDataEdit)).getText().toString();
                addServer(name, mainActivity.getGameUUIDFromName(game), ip, Integer.parseInt(playerCap), additionalData);
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


    private void addServer(String name, String gameUUID, String ip, Integer playerCap, String additionalData) {
        JSONObject userdata = new JSONObject();
        try {
            userdata.put("name", name);
            userdata.put("game", gameUUID);
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
        FGEncrypt fgEncrypt = new FGEncrypt();
        String encryptedUserdata = fgEncrypt.encrypt(userdata.toString());
        String finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, mainActivity.master);
        Log.d("userdata unencrypted", userdata.toString());
        Log.d("addserver final", finalQuery);
        new AddServerAsyncTask().execute("https://fewgamers.com/api/server/", finalQuery, "POST");
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

    private void requestMyServers() {
        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        String urlString = "https://fewgamers.com/api/server/?creator=" + mainActivity.uuid + "&key=" + mainActivity.master;
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Volley my servers", response);
                myServersList.clear();
                myServersList = getMyServerListFromString(response);

                currentServersList.clear();
                if (mainActivity.userTabSelected == 1) {
                    userAdapter.addAll(myServersList);
                }

                // laden is voorbij
                notReady = false;
                mainActivity.mainProgressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainActivity.mainProgressBar.setVisibility(View.GONE);
                ((TextView) getActivity().findViewById(R.id.userInfoNoServersFoundText)).setText("No servers found");
                toggleDisplaysVisibility(true);
                notReady = false;
            }
        });

        requestQueue.add(stringRequest);
    }

    public static ArrayList<ServerObject> getMyServerListFromString(String string) {
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

    private class AddServerAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            if (response[0].equals("201")) {
                try {
                    JSONObject newlyAddedJSON = new JSONObject(response[1]);
                    ServerObject newlyAddedServer = new ServerObject();
                    newlyAddedServer.defineServer(newlyAddedJSON);
                    userAdapter.add(newlyAddedServer);
                    noServersFoundText.setVisibility(View.GONE);
                } catch (JSONException exception) {
                    Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                }
            } else if (response[1].equals("{'error': 'unique userdata already exists'}")) {
                Toast.makeText(mainActivity, "Name or IP is already in use", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mainActivity, "Failed to add server", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
