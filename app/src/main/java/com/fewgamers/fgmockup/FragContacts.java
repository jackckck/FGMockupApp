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

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragContacts extends ListFragBase {
    ArrayList<ContactObject> currentContactsList, friendsList, pendingList, blockedList;

    String[] allRegisteredUsernames, allRegisteredUUIDs;

    ContactsListAdapter contactsAdapter;

    TabLayout.OnTabSelectedListener conctactsTabsSelect;

    FloatingActionButton contactsFAB;

    private boolean notReady = true;

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

        contactsQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // alle widgets ophalen;
        contactsFAB = getActivity().findViewById(R.id.contactsFAB);

        mainActivity = (MainActivity) getActivity();
        getAllRegisteredUsers();

        currentContactsList = new ArrayList<>();
        friendsList = new ArrayList<>();
        pendingList = new ArrayList<>();
        blockedList = new ArrayList<>();

        // laden begint
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        mainActivity.friendsTabs.setVisibility(View.VISIBLE);

        if (mainActivity.hasFriendListStored) {
            makeContactsLists(mainActivity.completeContactsListString);
        } else {
            new contactsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + mainActivity.uuid + "&key=" + mainActivity.master, null, "GET");
        }

        conctactsTabsSelect = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (notReady) {
                    return;
                }
                int fabImageResource = R.drawable.ic_add_friend;
                currentContactsList.clear();
                switch (tab.getPosition()) {
                    case 0:
                        currentContactsList.addAll(friendsList);
                        mainActivity.friendsTabSelected = 0;
                        break;
                    case 1:
                        currentContactsList.addAll(pendingList);
                        mainActivity.friendsTabSelected = 1;
                        break;
                    case 2:
                        currentContactsList.addAll(blockedList);
                        mainActivity.friendsTabSelected = 2;
                        fabImageResource = R.drawable.ic_block_person;
                        break;
                }
                contactsAdapter.notifyDataSetChanged();

                contactsFAB.setImageResource(fabImageResource);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };
        mainActivity.friendsTabs.addOnTabSelectedListener(conctactsTabsSelect);

        switch (mainActivity.friendsTabSelected) {
            case 0:
                contactsFAB.setVisibility(View.VISIBLE);
                contactsFAB.setImageResource(R.drawable.ic_add_friend);
                break;
            case 1:
                contactsFAB.setImageResource(R.drawable.ic_add_friend);
                break;
            case 2:
                contactsFAB.setVisibility(View.VISIBLE);
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

    private void openProfile(String uuid) {
        FragUserInfo fragment = new FragUserInfo();
        fragment.setFriendUUID(uuid);

        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            mainActivity.friendsTabs.setVisibility(View.GONE);
            ft.commit();
        }
    }

    private void removeFriend(String uuid) {
        removeUserrelation("FA", uuid);
    }

    private void acceptFriend(String uuid) {
        addUserRelation("FA", uuid);
    }

    private void unblockUser(String uuid) {
        removeUserrelation("B", uuid);
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
        switch (currentContactsList.get(position).getStatus()) {
            case "FA":
                optionsList = new String[2];
                optionsList[0] = openProfileOption;
                optionsList[1] = "Remove friend";
                contactClickOptionsBuilder.setItems(optionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openProfile(uuid);
                        } else if (which == 1) {
                            removeFriend(uuid);
                        }
                    }
                });
                break;
            case "FP":
                optionsList = new String[2];
                optionsList[0] = openProfileOption;
                optionsList[1] = "Accept friend request";
                contactClickOptionsBuilder.setItems(optionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openProfile(uuid);
                        } else if (which == 1) {
                            acceptFriend(uuid);
                        }
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
            default:
                optionsList = new String[1];
                break;
        }

        AlertDialog friendClickOptionsDialog = contactClickOptionsBuilder.create();
        friendClickOptionsDialog.show();
    }


    private void makeContactsLists(String jsonString) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Contacts list not found", "Something went wrong when loading contacts data");
            return;
        }

        contactsCount = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String uuid, status;
                uuid = jsonObject.keys().next();
                status = jsonObject.getString(uuid);
                addContact(uuid, status, selectContactsList(status));
                contactsCount++;
            } catch (JSONException exception) {
                Log.e("Friend object missing", "Friend object data incomplete");
            }
        }
    }

    private String[] extractRelation(JSONObject userRelation) {
        String contactUUID, user1, user2, relationStatus;
        contactUUID = "";
        user1 = "";
        user2 = "";
        relationStatus = "";
        try {
            user1 = removeHyphens(userRelation.getString("user1"));
            user2 = removeHyphens(userRelation.getString("user2"));
            relationStatus = userRelation.getString("status");
        } catch (JSONException exception) {
        }
        if (mainActivity.uuid.equals(user1)) {
            if (relationStatus.equals("FP")) {
                relationStatus = "null";
            } else {
                contactUUID = user2;
            }
        } else if (mainActivity.uuid.equals(user2)) {
            if (relationStatus.equals("B")) {
                relationStatus = "null";
            } else {
                contactUUID = user1;
            }
        }
        String[] res = new String[2];
        res[0] = relationStatus;
        res[1] = contactUUID;
        return res;
    }

    private ArrayList<ContactObject> selectContactsList(String relationStatus) {
        ArrayList<ContactObject> res;
        switch (relationStatus) {
            case "FA":
                return friendsList;
            case "FP":
                return pendingList;
            case "B":
                return blockedList;
            default:
                return null;
        }
    }


    private void addContact(String contactUUID, final String relationStatus, final ArrayList<ContactObject> selectedList) {
        if (selectedList == null) {
            return;
        }
        contactsQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?uuid=" + contactUUID + mainActivity.urlKey, new Response.Listener<String>() {
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
            displayContactsList();
        } else {
            addedCount++;
        }
    }

    private class contactsAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String response) {
            if (response.equals("error")) {
                mainActivity.mainProgressBar.setVisibility(View.GONE);
                Log.e("Userrelation error", "Something went wrong when requesting usserelations at https://fewgamers.com/api/userrelation/");
                TextView failedToLoadText = getActivity().findViewById(R.id.contactsFailedToLoad);
                failedToLoadText.setText("Failed to load contacts");
            } else {
                makeContactsLists(response);

                mainActivity.completeContactsListString = response;
                mainActivity.hasFriendListStored = true;
            }

        }
    }

    private void displayContactsList() {
        Log.d("Display method", "De display method wordt aangeroepen.");
        switch (mainActivity.friendsTabSelected) {
            case 0:
                currentContactsList.addAll(friendsList);
                break;
            case 1:
                currentContactsList.addAll(pendingList);
                break;
            case 2:
                currentContactsList.addAll(blockedList);
                break;
        }
        contactsAdapter = new ContactsListAdapter(getActivity(), currentContactsList);
        setListAdapter(contactsAdapter);

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
                int index = Arrays.asList(allRegisteredUsernames).indexOf(autoComplete.getText().toString());
                try {
                    addUserRelation(relationStatus, allRegisteredUUIDs[index]);
                } catch (ArrayIndexOutOfBoundsException exception) {
                    Toast.makeText(getActivity(), "User" + autoComplete.getText().toString() + " could not be found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogBuilder.show();
    }

    private void addUserRelation(String relationStatus, String uuid) {
        JSONObject userdata = new JSONObject();
        JSONObject finalQuery = new JSONObject();
        String encryptedUserdata;
        FGEncrypt fgEncrypt = new FGEncrypt();
        try {
            userdata.put("user1", mainActivity.uuid);
            userdata.put("user2", uuid);
            userdata.put("status", relationStatus);
        } catch (JSONException exception) {
            Log.e("Userdata JSON error", "Could not construct userdata JSONObject");
        }
        Log.d("userdata unencrypted", userdata.toString());
        encryptedUserdata = fgEncrypt.encrypt(userdata.toString());
        try {
            finalQuery.put("key", mainActivity.master);
            finalQuery.put("userdata", encryptedUserdata);
        } catch (JSONException exception) {
            Log.e("Add userrelation error", "Could not construct add userrelation JSONObject");
        }

        Log.d("finalQuery", finalQuery.toString());
        new FGAsyncTask().execute("https://fewgamers.com/api/userrelation/", finalQuery.toString(), "POST");
    }

    private void removeUserrelation(String relationStatus, String uuid) {
        contactsQueue.add(new StringRequest(Request.Method.GET,
                "https://fewgamers.com/api/userrelation/?key=" + mainActivity.master + "&user2=" + uuid +
                        "&status=" + relationStatus,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley relation", response);
                        try {
                            JSONObject relation = new JSONObject(response);
                            JSONObject finalQuery = new JSONObject();
                            finalQuery.put("key", mainActivity.master);
                            finalQuery.put("uuid", relation.getString("uuid"));

                            new FGAsyncTask().execute("https://fewgamers.com/api/userrelation/", finalQuery.toString(), "PATCH");
                        } catch (JSONException exception) {
                            Log.e("Remove relation error", "Something went wrong while constructing JSONObject from received string, " +
                                    "or while constructing JSONObject for userrelation removal");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }));
    }

    private void getAllRegisteredUsers() {
        contactsQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?key=" + mainActivity.master, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("volley all users", response);
                ArrayList<String> eligibleUsernames = new ArrayList<>();
                ArrayList<String> eligibleUUIDs = new ArrayList<>();
                int eligibleCount = 0;
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        if (user.getString("status").equals("A")) {
                            eligibleUsernames.add(user.getString("nickname"));
                            eligibleUUIDs.add(user.getString("uuid"));
                            eligibleCount++;
                        }
                    }
                    allRegisteredUsernames = new String[eligibleCount];
                    allRegisteredUUIDs = new String[eligibleCount];
                    for (int j = 0; j < eligibleCount; j++) {
                        allRegisteredUsernames[j] = eligibleUsernames.get(j);
                        allRegisteredUUIDs[j] = eligibleUUIDs.get(j);
                    }
                } catch (JSONException exception) {
                    Log.e("/api/user/ error", "Could not extract JSONArray of all registered users");
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