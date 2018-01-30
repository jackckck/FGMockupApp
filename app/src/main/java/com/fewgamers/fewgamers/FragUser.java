package com.fewgamers.fewgamers;

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

/**
 * Created by Administrator on 12/6/2017.
 */

// fragment that allows a user to see their own user information, a list of servers that they've
// added, and a list of favourite servers (currently not implemented in the FewGamers database)
public class FragUser extends ListFragBase {
    // currentServersList reflects the servers visible onscreen. myServersList is a complete list
    // of all the user's servers. favoriteServersList is a complete list of all favourite servers
    ArrayList<ServerObject> currentServersList, myServersList, favoriteServersList;

    ServerListAdapter userAdapter;

    TextView usernameText, emailText, firstNameText, lastNameText, usernameDisplay,
            emailDisplay, firstNameDisplay, lastNameDisplay, noServersFoundText;

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

        // the progress bar will disappear once all information is ready to be displayed
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        // initialize ArrayLists, and set the list adapter
        myServersList = new ArrayList<>();
        favoriteServersList = new ArrayList<>();
        currentServersList = new ArrayList<>();
        userAdapter = new ServerListAdapter(getActivity(), currentServersList);
        setListAdapter(userAdapter);

        // asynchronous process to retrieve own servers
        requestMyServers();

        // retrieving widgets
        usernameDisplay = mainActivity.findViewById(R.id.userinfo_username_display);
        emailDisplay = mainActivity.findViewById(R.id.userinfo_email_display);
        firstNameDisplay = mainActivity.findViewById(R.id.userinfo_firstname_display);
        lastNameDisplay = mainActivity.findViewById(R.id.userinfo_lastname_display);

        usernameText = mainActivity.findViewById(R.id.userinfo_username);
        emailText = mainActivity.findViewById(R.id.userinfo_email);
        firstNameText = mainActivity.findViewById(R.id.userinfo_firstname);
        lastNameText = mainActivity.findViewById(R.id.userinfo_lastname);

        noServersFoundText = mainActivity.findViewById(R.id.userInfoNoServersFoundText);

        userFAB = mainActivity.findViewById(R.id.addServerFAB);

        setProfileDisplays();

        // restores the user TabLayout to its former state
        mainActivity.fgTabs.getTabAt(mainActivity.userFragTabSelected).select();

        // handles user TabLayout selections
        mainActivity.fgTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                userTabChangeTo(tab.getPosition());
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
                switch (mainActivity.userFragTabSelected) {
                    case 1:
                        showAddServerDialog();
                        break;
                    case 2:
                        showAddFavouriteServerDialog();
                        break;
                }
            }
        });
    }

    // changes the screens contents based on the selected tab
    private void userTabChangeTo(int selected) {
        currentServersList.clear();
        switch (selected) {
            case 0:
                mainActivity.userFragTabSelected = 0;
                toggleProfileDisplaysVisibility(true);
                noServersFoundText.setVisibility(View.GONE);
                userFAB.setVisibility(View.GONE);
                break;
            case 1:
                mainActivity.userFragTabSelected = 1;
                toggleProfileDisplaysVisibility(false);
                currentServersList.addAll(myServersList);
                userFAB.setImageResource(R.drawable.ic_plus_sign);
                userFAB.setVisibility(View.VISIBLE);
                noServersFoundText.setVisibility(View.VISIBLE);
                break;
            case 2:
                mainActivity.userFragTabSelected = 2;
                toggleProfileDisplaysVisibility(false);
                currentServersList.addAll(favoriteServersList);
                userFAB.setImageResource(R.drawable.ic_favourite_white_24dp);
                userFAB.setVisibility(View.VISIBLE);
                noServersFoundText.setVisibility(View.GONE);
                break;
        }
        if (currentServersList.size() == 0) {
            noServersFoundText.setText("No servers added");
        }
        userAdapter.notifyDataSetChanged();
    }

    // clicking a server will allow the user to remove it, or edit some of its information
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] clickMyServerOptions = new String[2];

        ServerObject clickedServer = (ServerObject) l.getItemAtPosition(position);
        final String clickedServerUUID, clickedServerName, clickedServerGame, clickedServerPlayercap, clickedServerAdditionalInfo;
        clickedServerUUID = clickedServer.getServerUUID();
        clickedServerName = clickedServer.getServerName();
        clickedServerGame = mainActivity.getGameNameFromUUID(clickedServer.getGameUUID());
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

    // AlertDialog that allows the user to change their server's information by filling out EditTexts
    // and hitting the confirm button
    private void showEditServerDialog(final String uuid, String serverName, String serverGame, String serverPlayercap, String serverAdditionalInfo) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View editServerDialogView = inflater.inflate(R.layout.dialog_edit_server, null);
        AlertDialog.Builder editServerDialogBuilder = new AlertDialog.Builder(getActivity());
        editServerDialogBuilder.setView(editServerDialogView);

        // get custom AlertDialog layout widgets
        final EditText editServerName = (EditText) editServerDialogView.findViewById(R.id.edit_server_name);
        final MultiAutoCompleteTextView editServerGameAutoComplete = editServerDialogView.findViewById(R.id.edit_server_autocomplete);
        final EditText editServerPlayercap = (EditText) editServerDialogView.findViewById(R.id.edit_server_playercap);
        final EditText editServerAdditionalInfo = (EditText) editServerDialogView.findViewById(R.id.edit_server_additional_info);

        // upon opening, the AlertDialog's EditTexts will be filled with the server's current
        // information, suppled in the method's parameters
        editServerName.setText(serverName);
        editServerGameAutoComplete.setText(serverGame);
        editServerPlayercap.setText(serverPlayercap);
        editServerAdditionalInfo.setText(serverAdditionalInfo);

        // autocomplete textbox to find registered games more easily
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

        // clicking the confirm button sends the newly entered server information to the FewGamers
        // database through the editMyServer() method
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

    // sends updated server information the the FewGamers database
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
        String finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, mainActivity.getMaster());

        new ChangeMyServersListAsyncTask().execute("https://fewgamers.com/api/server/", finalQuery, "PATCH");
    }

    // requests that the server belonging to the given uuid be removed from the FewGamers database
    private void removeServer(String serverUUID) {
        JSONObject finalQueryJSONObject = new JSONObject();
        try {
            finalQueryJSONObject.put("key", mainActivity.getMaster());
            finalQueryJSONObject.put("uuid", serverUUID);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        new ChangeMyServersListAsyncTask().execute("https://fewgamers.com/api/server/", finalQueryJSONObject.toString(), "DELETE");
    }

    // extension of FGAsyncTask that will inform the user of a failed removal request, and that will
    // update the displayed server list upon completion
    private class ChangeMyServersListAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            if (Integer.parseInt(response[0]) >= 400) {
                if (response[0].equals("404")) {
                    Toast.makeText(getActivity(), "Server not found in database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Could not remove server", Toast.LENGTH_SHORT).show();
                }
            }
            requestMyServers();
        }
    }

    // shows an AlertDIalog that allows a user to add their own server
    private void showAddServerDialog() {
        // sets the dialog's view with a custom AlertDialog layout, containing EditTexts for the new
        // server's info
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View addServerDialogView = inflater.inflate(R.layout.dialog_add_server, null);
        AlertDialog.Builder addServerDialogBuilder = new AlertDialog.Builder(getActivity());
        addServerDialogBuilder.setView(addServerDialogView);

        // autocomplete textbox to help the user find registered games
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

        // once the user clicks the confirm button, the filled out server information is reviewed
        // if a mandatory field is missing, the user will be notified. otherwise, another AlertDialog
        // will open, asking the user to confirm their request
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
        addServerDialogBuilder.show();
    }

    // opens an AlertDialog that displays the server information given through the parameters. a user
    // can also add additional data through another EditText if the confirm button is clicked, a 
    // request will be made to the FewGamers database, to add a server with the displayed information
    private void showAddServerConfirmDialog(final View dialogView, final String name, final String game, final String ip, final String playerCap) {

        AlertDialog.Builder addServerConfirmDialogBuilder = new AlertDialog.Builder(getActivity());
        addServerConfirmDialogBuilder.setView(dialogView);
        addServerConfirmDialogBuilder.setTitle("Add the following server?");

        // retrieve the custom AlertDialog layout's widgets
        ((TextView) dialogView.findViewById(R.id.confirm_add_server_name)).setText(name);
        ((TextView) dialogView.findViewById(R.id.confirm_add_server_game)).setText(game);
        ((TextView) dialogView.findViewById(R.id.confirm_add_server_ip)).setText(ip);
        ((TextView) dialogView.findViewById(R.id.confirm_add_server_playercount)).setText(playerCap);

        // when confirmation button is clicked, the request to add the server will be made
        addServerConfirmDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String additionalData = ((EditText) dialogView.findViewById(R.id.confirm_add_server_additionaldata)).getText().toString();
                addServer(name, mainActivity.getGameUUIDFromName(game), ip, Integer.parseInt(playerCap), additionalData);
            }
        });
        addServerConfirmDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        addServerConfirmDialogBuilder.show();
    }

    // since there is no favourites feature yet, no dialog to add a favourite server exists
    private void showAddFavouriteServerDialog() {
    }

    // sends a request to add a server that has all the information supplid by the method's parameters
    private void addServer(String name, String gameUUID, String ip, Integer playerCap, String additionalData) {
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        JSONObject userdata = new JSONObject();
        try {
            userdata.put("name", name);
            userdata.put("game", gameUUID);
            userdata.put("ip", ip);
            userdata.put("creator", mainActivity.getMyUuid());
            if (playerCap.equals("")) {
                userdata.put("playercount", "None");
            } else {
                userdata.put("playercount", playerCap);
            }
            userdata.put("additionaldata", additionalData);
        } catch (JSONException exception) {
            mainActivity.mainProgressBar.setVisibility(View.GONE);
            Log.e("addServer JSONException", "Something went wrong when constructing addServer userdata JSONObject");
        }
        FGEncrypt fgEncrypt = new FGEncrypt();
        String encryptedUserdata = fgEncrypt.encrypt(userdata.toString());
        String finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, mainActivity.getMaster());
        new AddServerAsyncTask().execute("https://fewgamers.com/api/server/", finalQuery, "POST");
    }

    // used to toggle the visibility of all the user info displayed in the profile tab
    private void toggleProfileDisplaysVisibility(boolean b) {
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

    // gets the user's user data stored in the MainActivity, and displays it on the user's profile
    private void setProfileDisplays() {
        usernameDisplay.setText(mainActivity.getMyUsername());
        emailDisplay.setText(mainActivity.getMyEmail());
        firstNameDisplay.setText(mainActivity.getMyFirstName());
        lastNameDisplay.setText(mainActivity.getMyLastName());
    }

    // retrieves the user's posted servers from the FewGamers database, and creates the required
    // ArrayList<ServerObject>
    private void requestMyServers() {
        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        String urlString = "https://fewgamers.com/api/server/?creator=" + mainActivity.getMyUuid() + "&key=" + mainActivity.getMaster();
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                myServersList.clear();
                myServersList = getMyServerListFromJSONArray(response);

                userTabChangeTo(mainActivity.userFragTabSelected);

                // laden is voorbij
                mainActivity.mainProgressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainActivity.mainProgressBar.setVisibility(View.GONE);
                ((TextView) getActivity().findViewById(R.id.userInfoNoServersFoundText)).setText("No servers found");
                toggleProfileDisplaysVisibility(true);
            }
        });

        requestQueue.add(stringRequest);
    }

    // creates and returns an ArrayList<ServerObject> from a given JSONArray, without sorting its
    // elements
    public static ArrayList<ServerObject> getMyServerListFromJSONArray(String string) {
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
            exception.printStackTrace();
        }
        return res;
    }

    // extension of FGAsyncTask, that informs the user of potential errors. when succesful, instead
    // of requesting all of the user's added servers again, the response body is used to construct
    // an identical copy of the added ServerObject, which is added to the servers displayed onscreen
    private class AddServerAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            if (response[0].equals("201")) {
                try {
                    JSONObject newlyAddedJSON = new JSONObject(response[1]);
                    ServerObject newlyAddedServer = new ServerObject();
                    newlyAddedServer.defineServer(newlyAddedJSON);
                    userAdapter.add(newlyAddedServer);
                    noServersFoundText.setText("");
                } catch (JSONException exception) {
                    Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                }
            } else if (response[1].equals("{'error': 'unique userdata already exists'}")) {
                Toast.makeText(mainActivity, "Name or IP is already in use", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mainActivity, "Failed to add server", Toast.LENGTH_SHORT).show();
            }
            mainActivity.mainProgressBar.setVisibility(View.GONE);
        }
    }
}
