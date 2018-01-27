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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragContacts extends ListFragBase {
    ArrayList<ContactObject> currentContactsList, friendsList, pendingOutgoingList, pendingIncomingList, blockedList;

    String[] allRegisteredUsernames, allRegisteredEmails, allRegisteredUUIDs;
    Map<String, String> registeredUUIDsMap = new HashMap<>();

    ContactsListAdapter contactsAdapter;

    TextView failedToLoadText;

    FloatingActionButton contactsFAB;

    private boolean notReady = true;
    private boolean noFriends, noPending, noBlocked;

    RequestQueue contactsQueue;

    int contactsCount = 0;
    int addedCount = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragcontacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allRegisteredUsernames = new String[1];
        allRegisteredUsernames[0] = "No users found";

        contactsQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // alle widgets ophalen;
        contactsFAB = getActivity().findViewById(R.id.contactsFAB);
        failedToLoadText = getActivity().findViewById(R.id.contactsFailedToLoad);

        mainActivity = (MainActivity) getActivity();
        getAllRegisteredUsers();

        currentContactsList = new ArrayList<>();
        friendsList = new ArrayList<>();
        pendingOutgoingList = new ArrayList<>();
        ContactObject outgoingHeader = new ContactObject();
        outgoingHeader.setAsHeader("My friend requests:");
        pendingOutgoingList.add(outgoingHeader);
        pendingIncomingList = new ArrayList<>();
        ContactObject incomingHeader = new ContactObject();
        incomingHeader.setAsHeader("Incoming friend requests:");
        pendingIncomingList.add(incomingHeader);
        blockedList = new ArrayList<>();
        contactsAdapter = new ContactsListAdapter(getActivity(), currentContactsList);
        setListAdapter(contactsAdapter);

        // laden begint
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        mainActivity.friendsTabs.setVisibility(View.VISIBLE);

        //new GetRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + mainActivity.uuid + mainActivity.urlKey, null, "GET");
        new DemoGetRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + mainActivity.uuid + mainActivity.urlKey, null, "GET");

        mainActivity.friendsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (notReady) {
                    return;
                }
                displayContactsList(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //mainActivity.friendsTabs.getTabAt(mainActivity.friendsTabSelected).select();
        switch (mainActivity.friendsTabSelected) {
            case 0:
                contactsFAB.setImageResource(R.drawable.ic_add_friend);
                break;
            case 1:
                contactsFAB.setImageResource(R.drawable.ic_add_friend);
                break;
            case 2:
                contactsFAB.setImageResource(R.drawable.ic_block_person);
                break;
        }

        contactsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserrelationDialog(mainActivity.friendsTabSelected);
            }
        });
    }

    public static void openProfile(String uuid, MainActivity mainActivity) {
        FragUserInfo fragment = new FragUserInfo();
        fragment.setFriendUUID(uuid);

        if (fragment != null) {
            mainActivity.previousFragId.add(0, R.integer.display_fragment_user_info_id);
            mainActivity.onBackUUIDs.add(0, uuid);

            FragmentTransaction ft = mainActivity.getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            mainActivity.friendsTabs.setVisibility(View.GONE);
            ft.commit();
        }
    }

    private void removeFriend(String uuid) {
        removeUserrelation(uuid);
    }

    private void acceptFriend(String uuid) {
        addUserRelation("FA", uuid);
    }

    private void unblockUser(String uuid) {
        removeUserrelation(uuid);
    }

    private void openChatActivity() {
        Intent moveToChatIntent = new Intent(getActivity(), ChatActivity.class);
        startActivity(moveToChatIntent);
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        AlertDialog.Builder contactClickOptionsBuilder = new AlertDialog.Builder(getActivity());

        String[] optionsList;
        String openProfileOption = currentContactsList.get(position).getUsername() + "'s profile";
        final String uuid = currentContactsList.get(position).getUUID();
        switch (currentContactsList.get(position).getRelationStatus()) {
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
            case "incomingFP":
                optionsList = new String[2];
                optionsList[0] = openProfileOption;
                optionsList[1] = "Accept friend request";
                contactClickOptionsBuilder.setItems(optionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openProfile(uuid, mainActivity);
                        } else if (which == 1) {
                            acceptFriend(uuid);
                        }
                    }
                });
                break;
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


    private void makeContactsLists(String jsonString) {
        failedToLoadText.setText("");
        clearContactLists();
        if (jsonString.equals("[]")) {
            failedToLoadText.setText("No contacts found");
            mainActivity.mainProgressBar.setVisibility(View.GONE);
            notReady = false;
        }
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Contacts list not found", "Something went wrong when loading contacts data");
            return;
        }

        addedCount = 0;
        contactsCount = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject relationJSONObject = jsonArray.getJSONObject(i);
                String uuid, status;
                uuid = relationJSONObject.keys().next();
                status = relationJSONObject.getString(uuid);
                addContactToList(uuid, status, selectContactsList(status));
                contactsCount++;
            } catch (JSONException exception) {
                Log.e("Friend object missing", "Friend object data incomplete");
            }
        }
    }

    private void clearContactLists() {
        friendsList.clear();
        pendingOutgoingList.subList(1, pendingOutgoingList.size()).clear();
        pendingIncomingList.subList(1, pendingIncomingList.size()).clear();
        blockedList.clear();
        contactsAdapter.clear();
    }

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


    private void addContactToList(String contactUUID, final String relationStatus, final ArrayList<ContactObject> selectedList) {
        if (selectedList == null) {
            return;
        }
        contactsQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?uuid=" + contactUUID + "&key=" + mainActivity.master, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("volley response", response);
                ContactObject contactObject = new ContactObject();
                contactObject.defineContact(response, relationStatus);
                selectedList.add(contactObject);

                checkIfDone();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }));
    }

    private void checkIfDone() {
        if (addedCount + 1 == contactsCount) {
            displayContactsList(mainActivity.friendsTabSelected);
        } else {
            addedCount++;
        }
    }

    private class GetRelationsAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            if (Integer.parseInt(response[0]) >= 400) {
                Log.e("Userrelation error", "Something went wrong when requesting usserelations at https://fewgamers.com/api/userrelation/");
                failedToLoadText.setText("Failed to load contacts");
            } else {
                makeContactsLists(response[1]);
                mainActivity.completeContactsListString = response[1];
                mainActivity.hasFriendListStored = true;
            }
            // laden is voorbij
            mainActivity.mainProgressBar.setVisibility(View.GONE);
        }
    }

    private class DemoGetRelationsAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            try {
                final JSONArray myRelations = new JSONArray(response[1]);

                RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
                StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://fewgamers.com/api/userrelation/?key=" + mainActivity.key + "&user2=" + mainActivity.uuid + "&status=FP", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("relevant relations", myRelations.toString());
                            JSONArray userTwoPendingRelations = new JSONArray(response);
                            for (int i = 0; i < userTwoPendingRelations.length(); i++) {
                                JSONObject incomingPendingRelationObject = userTwoPendingRelations.getJSONObject(i);
                                String uuid = incomingPendingRelationObject.getString("user1");

                                JSONObject relevantPendingRelationObject = new JSONObject();
                                relevantPendingRelationObject.put(uuid, "incomingFP");
                                myRelations.put(relevantPendingRelationObject);
                            }
                            makeContactsLists(myRelations.toString());
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

    private class ChangeRelationsAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            if (response[0].equals("201") || response[0].equals("200")) {
                //new GetRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + mainActivity.uuid + "&key=" + mainActivity.master, null, "GET");
                new DemoGetRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + mainActivity.uuid + mainActivity.urlKey, null, "GET");
            } else if (response[1].equals("{'error': 'unique userdata already exists'}")) {
                if (mainActivity.friendsTabSelected == 2) {
                    Toast.makeText(getActivity(), "Block already in place", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Friend request still pending", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Could not add userrelation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayContactsList(int tabSelected) {
        failedToLoadText.setText("");
        int fabImageResource = R.drawable.ic_add_friend;
        currentContactsList.clear();
        switch (tabSelected) {
            case 0:
                if (friendsList.size() == 0) {
                    failedToLoadText.setText("No friends yet");
                } else {
                    currentContactsList.addAll(friendsList);
                }
                mainActivity.friendsTabSelected = 0;
                break;
            case 1:
                if (pendingOutgoingList.size() + pendingIncomingList.size() == 2) {
                    failedToLoadText.setText("No pending friend requests");
                }
                if (pendingOutgoingList.size() > 1) {
                    currentContactsList.addAll(pendingOutgoingList);
                }
                if (pendingIncomingList.size() > 1) {
                    currentContactsList.addAll(pendingIncomingList);
                }
                mainActivity.friendsTabSelected = 1;
                break;
            case 2:
                if (blockedList.size() == 0) {
                    failedToLoadText.setText("No blocked users");
                } else {
                    currentContactsList.addAll(blockedList);
                }
                mainActivity.friendsTabSelected = 2;
                fabImageResource = R.drawable.ic_block_person;
                break;
        }
        contactsFAB.setImageResource(fabImageResource);

        contactsAdapter.notifyDataSetChanged();

        // laden is voorbij
        mainActivity.mainProgressBar.setVisibility(View.GONE);

        notReady = false;
    }

    private void showUserrelationDialog(final int friendsTabSelected) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.add_userrelation_alertdialog, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, allRegisteredUsernames);

        final AutoCompleteTextView autoComplete = (AutoCompleteTextView) dialogView.findViewById(R.id.add_userrelation_autocomplete);
        autoComplete.setAdapter(adapter);
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUser = (String) parent.getItemAtPosition(position);
                autoComplete.setText(selectedUser);
            }
        });

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
                if (uuid == null) {
                    addUserRelation(relationStatus, userIdentifier);
                } else {
                    addUserRelation(relationStatus, uuid);
                }
            }
        });

        dialogBuilder.show();
    }

    private void addUserRelation(String relationStatus, String uuid) {
        JSONObject userDataJSONObject = new JSONObject();
        String finalQuery;
        String encryptedUserdata;
        FGEncrypt fgEncrypt = new FGEncrypt();
        try {
            userDataJSONObject.put("user1", mainActivity.uuid);
            userDataJSONObject.put("user2", uuid);
            userDataJSONObject.put("status", relationStatus);
        } catch (JSONException exception) {
            Log.e("Userdata JSON error", "Could not construct userdata JSONObject");
        }
        Log.d("userdata unencrypted", userDataJSONObject.toString());
        encryptedUserdata = fgEncrypt.encrypt(userDataJSONObject.toString());
        finalQuery = fgEncrypt.getFinalQuery(encryptedUserdata, mainActivity.master);

        Log.d("finalQuery", finalQuery);
        new ChangeRelationsAsyncTask().execute("https://fewgamers.com/api/userrelation/", finalQuery, "POST");
    }

    private void removeUserrelation(String uuid) {
        contactsQueue.add(new StringRequest(Request.Method.GET,
                "https://fewgamers.com/api/userrelation/?key=" + mainActivity.master + "&user2=" + uuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley relation", response);
                        try {
                            JSONObject relation = new JSONObject(response.substring(1, response.length()));
                            JSONObject finalQuery = new JSONObject();
                            finalQuery.put("key", mainActivity.master);
                            finalQuery.put("uuid", relation.getString("uuid"));

                            Log.d("finalQuery", finalQuery.toString());
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

    private void getAllRegisteredUsers() {
        contactsQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?key=" + mainActivity.key, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d("volley all users", response);
                ArrayList<String> eligibleEmails = new ArrayList<>();
                ArrayList<String> eligibleUsernames = new ArrayList<>();
                ArrayList<String> eligibleUUIDs = new ArrayList<>();
                int eligibleCount = 0;
                int eligibleEmailCount = 0;
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        if (user.getString("status").equals("A")) {
                            String username, uuid;
                            username = user.getString("nickname");
                            uuid = user.getString("uuid");
                            eligibleUsernames.add(user.getString("nickname"));
                            eligibleUUIDs.add(user.getString("uuid"));
                            registeredUUIDsMap.put(username, uuid);

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