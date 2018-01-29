package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AutoCompleteTextView;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragContacts extends ListFragBase {
    // there is a ArrayList<ContactObject> for each relation type. the pending list is divided into
    // a list of contacts who have sent friendship requests to the user, and vice versa
    ArrayList<ContactObject> currentContactsList, friendsList, pendingOutgoingList, pendingIncomingList, blockedList;

    // arrays of all registered usernames, emails and the uuids that correspond to them.
    // a map that links every username and email to that user's uuid
    String[] allRegisteredUsernames, allRegisteredEmails, allRegisteredUUIDs;
    Map<String, String> registeredUUIDsMap = new HashMap<>();

    ContactsListAdapter contactsAdapter;

    TextView failedToLoadContactsText;
    FloatingActionButton contactsFAB;

    RequestQueue contactsQueue;

    // a count of all relations the user has, and the amount of contacts that have been added across
    // all ArrayLists. once the two ints are equal, the app is finished retrieving userdata for each
    // relation
    int relationsCount = 0;
    int contactsAddedCount = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragcontacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        mainActivity.fgTabs.setVisibility(View.VISIBLE);

        // to avoid NullPointerExceptions
        allRegisteredUsernames = new String[0];

        contactsQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // retrieving widgets
        contactsFAB = getActivity().findViewById(R.id.contactsFAB);
        // textview at the screen's center to indicate errors, and empty contacts lists
        failedToLoadContactsText = getActivity().findViewById(R.id.contactsFailedToLoad);

        getAllRegisteredUsers();

        // initializes the ArrayLists, after which contacts will be added asynchronously to each
        currentContactsList = new ArrayList<>();
        friendsList = new ArrayList<>();
        pendingOutgoingList = new ArrayList<>();
        pendingIncomingList = new ArrayList<>();
        blockedList = new ArrayList<>();

        // creates the header ContactObjects within the pending list
        ContactObject outgoingHeader = new ContactObject(null, null);
        outgoingHeader.setAsHeader("My friend requests:");
        pendingOutgoingList.add(outgoingHeader);

        ContactObject incomingHeader = new ContactObject(null, null);
        incomingHeader.setAsHeader("Incoming friend requests:");
        pendingIncomingList.add(incomingHeader);

        // initializing adapter. any change to currentContactsList will be reflected onscreen
        contactsAdapter = new ContactsListAdapter(getActivity(), currentContactsList);
        setListAdapter(contactsAdapter);

        // asynchronous progress begins
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);
        refreshContactsLists();

        // click handler for the TabLayout
        mainActivity.fgTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // changes the contents of the screen based on the selected tab
                displayContactsList(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // restores the TabLayout's previous selection
        mainActivity.fgTabs.getTabAt(mainActivity.contactsFragTabSelected).select();

        // a button to add uer relations
        contactsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserrelationDialog(mainActivity.contactsFragTabSelected);
            }
        });
    }

    // method that finds a user by their uuid, and opens their profile
    public static void openProfile(String uuid, MainActivity mainActivity) {
        FragUserInfo fragment = new FragUserInfo();
        fragment.setUserUUID(uuid);

        if (fragment != null) {
            mainActivity.previousFragId.add(0, R.integer.display_fragment_user_info_id);

            // stores the uuid for the MainActivity's onBackPressed override
            mainActivity.onBackUUIDs.add(0, uuid);

            FragmentTransaction ft = mainActivity.getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            mainActivity.fgTabs.setVisibility(View.GONE);
            ft.commit();
        }
    }

    // not in use currently
    private void openChatActivity() {
        Intent moveToChatIntent = new Intent(getActivity(), ChatActivity.class);
        startActivity(moveToChatIntent);
    }

    // handles a user's clicks on one of their contacts
    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        AlertDialog.Builder contactClickOptionsBuilder = new AlertDialog.Builder(getActivity());

        // the strings in this array will be displayed as options for the user
        String[] optionsList;
        String openProfileOption = currentContactsList.get(position).getUsername() + "'s profile";
        final String uuid = currentContactsList.get(position).getUUID();
        switch (currentContactsList.get(position).getRelationStatus()) {
            // options for a friend contact
            case "FA":
                optionsList = new String[2];
                optionsList[0] = openProfileOption;
                optionsList[1] = "Remove friend";
                contactClickOptionsBuilder.setItems(optionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openProfile(uuid, mainActivity);
                        } else if (which == 1) {
                            removeFriend(uuid);
                        }
                    }
                });
                break;
            // options for another user's friend request
            case "incomingFP":
                optionsList = new String[3];
                optionsList[0] = openProfileOption;
                optionsList[1] = "Accept";
                optionsList[2] = "Deny";
                contactClickOptionsBuilder.setItems(optionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                openProfile(uuid, mainActivity);
                                break;
                            case 1:
                                acceptFriend(uuid);
                                break;
                            case 2:
                                denyFriendRequest(uuid);
                                break;
                        }
                    }
                });
                break;
            // option to cancel own friend requests
            case "FP":
                optionsList = new String[1];
                optionsList[0] = "Cancel";
                contactClickOptionsBuilder.setItems(optionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeUserrelation(uuid);
                    }
                });
                break;
            // option to undo the block on a user
            case "B":
                optionsList = new String[1];
                optionsList[0] = "Unblock";
                contactClickOptionsBuilder.setItems(optionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            unblockUser(uuid);
                        }
                    }
                });
                break;
        }

        AlertDialog friendClickOptionsDialog = contactClickOptionsBuilder.create();
        friendClickOptionsDialog.show();
    }

    private void removeFriend(String uuid) {
        removeUserrelation(uuid);
    }

    private void denyFriendRequest(String uuid) {
        removeUserrelation(uuid);
    }

    private void acceptFriend(String uuid) {
        addUserRelation("FA", uuid);
    }

    private void unblockUser(String uuid) {
        removeUserrelation(uuid);
    }

    // sorts the relations inside the specified JSONArray by their relation status. for each relation
    // a ContactObject is added to the right contacts list
    private void sortRelationsByList(String jsonString) {
        failedToLoadContactsText.setText("");
        clearContactLists();
        if (jsonString.equals("[]")) {
            displayContactsList(mainActivity.contactsFragTabSelected);
        }
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Relations JSONArray", "Something was wrong with the relations JSONArray" +
                    "from https://fewgamers.com/api/userrelation/");
            exception.printStackTrace();
            return;
        }

        contactsAddedCount = 0;
        relationsCount = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject relationJSONObject = jsonArray.getJSONObject(i);
                String uuid, status;
                uuid = relationJSONObject.keys().next();
                status = relationJSONObject.getString(uuid);
                // calls method to add a contactObject to the right list
                addContactToList(uuid, status, selectContactsList(status));
                relationsCount++;
            } catch (JSONException exception) {
                Log.e("Friend object missing", "Friend object data incomplete");
            }
        }
    }

    // clears contact lists of all items except headers
    private void clearContactLists() {
        friendsList.clear();
        pendingOutgoingList.subList(1, pendingOutgoingList.size()).clear();
        pendingIncomingList.subList(1, pendingIncomingList.size()).clear();
        blockedList.clear();
        contactsAdapter.clear();
    }

    // returns the right contacts list for the specified relation status
    private ArrayList<ContactObject> selectContactsList(String relationStatus) {
        switch (relationStatus) {
            case "FA":
                return friendsList;
            case "FP":
                return pendingOutgoingList;
            case "incomingFP":
                return pendingIncomingList;
            case "B":
                return blockedList;
            default:
                return null;
        }
    }

    // retrieves a user's userdata from their uuid, and constructs a ContactObject from it. this
    // ContactObject is added to the specified contacts list
    private void addContactToList(String contactUUID, final String relationStatus, final ArrayList<ContactObject> selectedList) {
        if (selectedList == null) {
            return;
        }
        contactsQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?uuid=" + contactUUID + "&key=" + mainActivity.getMaster(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("volley response", response);
                ContactObject contactObject = new ContactObject(response, relationStatus);
                selectedList.add(contactObject);

                checkIfDone();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }));
    }

    // checks if the total amount of user relations and total amount of added contacts are equal
    // if true, it displays the contacts list on the user's screen. this is done to synchronize
    // the asynchronous volley requests
    private void checkIfDone() {
        if (contactsAddedCount + 1 == relationsCount) {
            displayContactsList(mainActivity.contactsFragTabSelected);
        } else {
            contactsAddedCount++;
        }
    }

    // an extension of FGAsyncTask that overrides its onPostExecute
    private class GetRelationsAsyncTask extends FGAsyncTask {
        // this class uses FGAsyncTask's doInBackground() to retrieve all userrelations from
        // https://fewgamers.com/api/userrelation/?user1= + this user's uuid. then it uses a volley
        // GET request to retrieve all userrelations in which this user is user2. the sum of these
        // userrelations is used to create the ArrayLists
        @Override
        protected void onPostExecute(String[] response) {
            // this handles the event that a user has no internet connection
            if (response[1] == null) {
                failedToLoadContactsText.setText("Could not retrieve contacts");
                mainActivity.mainProgressBar.setVisibility(View.GONE);
                return;
            }
            try {
                final JSONArray myRelations = new JSONArray(response[1]);

                RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
                StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://fewgamers.com/api/userrelation/?key=" + mainActivity.getMyKey() + "&user2=" + mainActivity.getMyUuid() + "&status=FP", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("relevant relations", myRelations.toString());
                            JSONArray userTwoPendingRelations = new JSONArray(response);
                            // the retrieved user2 userrelations are formatted differently than the
                            // user1 userrelations
                            for (int i = 0; i < userTwoPendingRelations.length(); i++) {
                                JSONObject incomingPendingRelationObject = userTwoPendingRelations.getJSONObject(i);
                                String uuid = incomingPendingRelationObject.getString("user1");

                                JSONObject relevantPendingRelationObject = new JSONObject();
                                relevantPendingRelationObject.put(uuid, "incomingFP");
                                myRelations.put(relevantPendingRelationObject);
                            }
                            Log.d("my relations", myRelations.toString());
                            sortRelationsByList(myRelations.toString());
                        } catch (JSONException exception) {
                            exception.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Server error", error.toString());
                    }
                });
                requestQueue.add(stringRequest);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    // changes what the fragment displays depended on the selected tab
    private void displayContactsList(int tabSelected) {
        failedToLoadContactsText.setText("");
        int fabImageResource = R.drawable.ic_add_friend;

        // currentContactsList is emptied, and the relevant contacts list is added
        // if the blocked list is displayed, the floating action button is given a block symbol
        currentContactsList.clear();
        switch (tabSelected) {
            case 0:
                if (friendsList.size() == 0) {
                    failedToLoadContactsText.setText("No friends yet");
                } else {
                    currentContactsList.addAll(friendsList);
                }
                mainActivity.contactsFragTabSelected = 0;
                break;
            case 1:
                if (pendingOutgoingList.size() + pendingIncomingList.size() == 2) {
                    failedToLoadContactsText.setText("No pending friend requests");
                }
                if (pendingOutgoingList.size() > 1) {
                    currentContactsList.addAll(pendingOutgoingList);
                }
                if (pendingIncomingList.size() > 1) {
                    currentContactsList.addAll(pendingIncomingList);
                }
                mainActivity.contactsFragTabSelected = 1;
                break;
            case 2:
                if (blockedList.size() == 0) {
                    failedToLoadContactsText.setText("No blocked users");
                } else {
                    currentContactsList.addAll(blockedList);
                }
                mainActivity.contactsFragTabSelected = 2;
                fabImageResource = R.drawable.ic_block_person;
                break;
        }
        contactsFAB.setImageResource(fabImageResource);
        contactsFAB.setVisibility(View.VISIBLE);

        contactsAdapter.notifyDataSetChanged();

        // asynchronous process is over
        mainActivity.mainProgressBar.setVisibility(View.GONE);
    }

    // shows a dialog for adding user relations. the contents of the dialog depend on the selected tab
    private void showUserrelationDialog(final int friendsTabSelected) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        // inflates custom layout and attaches it to the AlertDialog.Builder
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.add_userrelation_alertdialog, null);
        dialogBuilder.setView(dialogView);

        // initializes an autocomplete textbox for selecting users
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, allRegisteredUsernames);
        final AutoCompleteTextView autoComplete = (AutoCompleteTextView) dialogView.findViewById(R.id.add_userrelation_autocomplete);
        autoComplete.setAdapter(adapter);
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = (String) parent.getItemAtPosition(position);
                autoComplete.setText(selectedUser);
            }
        });

        // determines the dialog's button texts. relationStatus is passed on to addUserrelation()
        final String dialogTitle, positiveText, relationStatus;
        if (friendsTabSelected == 2) {
            dialogTitle = "Block user";
            positiveText = "Block";
            relationStatus = "B";
        } else {
            dialogTitle = "Add friend";
            positiveText = "Add";
            relationStatus = "FP";
        }
        dialogBuilder.setTitle(dialogTitle);
        dialogBuilder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userIdentifier = autoComplete.getText().toString();
                String uuid = registeredUUIDsMap.get(userIdentifier);

                // als er een uuid gevonden kan worden bij wat de gebruiker heeft ingevuld, wordt
                // deze opgestuurd naar de server. anders wordt de ingevulde string opgestuurd.
                if (uuid == null) {
                    addUserRelation(relationStatus, userIdentifier);
                } else {
                    addUserRelation(relationStatus, uuid);
                }
            }
        });

        dialogBuilder.show();
    }

    // requests the user to post a user relation between this user's uuid, and that of the new contact
    private void addUserRelation(String relationStatus, String uuid) {
        JSONObject userDataJSONObject = new JSONObject();
        String finalQuery;
        String encryptedUserdata;
        FGEncrypt fgEncrypt = new FGEncrypt();
        // the userdata string looks like: {"user1":eigen uuid, "user2":andere uuid, "status":relation status}
        try {
            userDataJSONObject.put("user1", mainActivity.getMyUuid());
            userDataJSONObject.put("user2", uuid);
            userDataJSONObject.put("status", relationStatus);
        } catch (JSONException exception) {
            Log.e("Userdata JSON error", "Could not construct userdata JSONObject");
        }
        Log.d("userdata unencrypted", userDataJSONObject.toString());

        encryptedUserdata = fgEncrypt.encrypt(userDataJSONObject.toString());
        finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, mainActivity.getMaster());

        Log.d("finalQuery", finalQuery);

        // this is sent to the server: {"key":master key, "userdata":encrypted userdata}
        new ChangeRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/", finalQuery, "POST");
    }

    // removes the relation with the user who has the specified uuid
    private void removeUserrelation(String uuid) {
        // looks up the relevant user relation through, and requests that it be removed with an
        // AsyncTask
        contactsQueue.add(new StringRequest(Request.Method.GET,
                "https://fewgamers.com/api/userrelation/?key=" + mainActivity.getMaster() + "&user2=" + uuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley relation", response);
                        try {
                            JSONObject relation = new JSONObject(response.substring(1, response.length()));
                            JSONObject finalQuery = new JSONObject();
                            finalQuery.put("key", mainActivity.getMaster());
                            finalQuery.put("uuid", relation.getString("uuid"));

                            Log.d("finalQuery", finalQuery.toString());

                            // stuurt de volgende jsonstring naar de server: {"key":master key, "uuid":uuid van de relatie}
                            new ChangeRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/", finalQuery.toString(), "DELETE");
                        } catch (JSONException exception) {
                            Log.e("Remove relation error", "Something went wrong while constructing JSONObject from received string, " +
                                    "or while constructing JSONObject for userrelation removal");
                            exception.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }));
    }

    // extension of FGAsyncTask that handles various error responses. this class is used for adding
    // and removing user relations
    private class ChangeRelationsAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            // refreshes the contacts
            if (response[0].equals("201") || response[0].equals("200")) {
                refreshContactsLists();
            } else if (response[1].equals("{'error': 'unique userdata already exists'}")) {
                if (mainActivity.contactsFragTabSelected == 2) {
                    Toast.makeText(getActivity(), "Block already in place", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Friend request still pending", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Could not add userrelation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshContactsLists() {
        new GetRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + mainActivity.getMyUuid() + "&key=" + mainActivity.getMyKey(), null, "GET");
    }

    // retrieves all active FewGamers users, and constructs three arrays that contain those users'
    // emails, usernames and uuids. a map is also constructed to retrieve uuids, with emails and
    // usernames as keys
    private void getAllRegisteredUsers() {
        contactsQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?key=" + mainActivity.getMyKey(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("volley all users", response);
                ArrayList<String> eligibleEmails = new ArrayList<>();
                ArrayList<String> eligibleUsernames = new ArrayList<>();
                ArrayList<String> eligibleUUIDs = new ArrayList<>();
                int eligibleCount = 0;
                int eligibleEmailCount = 0;
                try {
                    // JSONArray that contains all FewGamers users
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        // only active accounts are considered
                        if (user.getString("status").equals("A")) {
                            String username, uuid;
                            username = user.getString("nickname");
                            uuid = user.getString("uuid");
                            eligibleUsernames.add(user.getString("nickname"));
                            eligibleUUIDs.add(user.getString("uuid"));
                            registeredUUIDsMap.put(username, uuid);

                            // a private email will lead to a JSONException
                            try {
                                String email = user.getString("email");
                                eligibleEmails.add(email);
                                registeredUUIDsMap.put(email, uuid);
                                eligibleEmailCount++;
                            } catch (JSONException exception) {
                                Log.d(username, "email private");
                            }
                            eligibleCount++;
                        }
                    }
                    // creates arrays from the created ArrayLists
                    allRegisteredEmails = new String[eligibleEmailCount];
                    allRegisteredUsernames = new String[eligibleCount];
                    allRegisteredUUIDs = new String[eligibleCount];
                    for (int j = 0; j < eligibleEmailCount; j++) {
                        allRegisteredEmails[j] = eligibleEmails.get(j);
                    }
                    for (int k = 0; k < eligibleCount; k++) {
                        allRegisteredUsernames[k] = eligibleUsernames.get(k);
                        allRegisteredUUIDs[k] = eligibleUUIDs.get(k);
                    }
                } catch (JSONException exception) {
                    Log.e("/api/user/ error", "Could not extract JSONArray of all registered users");
                    exception.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }));
    }
}