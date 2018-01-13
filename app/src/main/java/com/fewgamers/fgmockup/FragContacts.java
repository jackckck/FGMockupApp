package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragContacts extends ListFragBase {
    ArrayList<ContactObject> currentContactsList, friendsList, pendingList, blockedList;

    String[] friendLongClickOptionsList = new String[3];
    String[] pendingLongClickOptionsList = new String[3];
    String[] blockedLongClickOptionsList = new String[1];

    FriendListAdapter friendAdapter;

    String thisUser = "082894cf-2c7c-4b30-87ff-806792804dfe";

    MainActivity mainActivity;

    Map<String, ArrayList<ContactObject>> contactsListMap;

    TabLayout.OnTabSelectedListener conctactsTabsSelect;

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
        contactsQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
        currentContactsList = new ArrayList<>();
        friendsList = new ArrayList<>();
        pendingList = new ArrayList<>();
        blockedList = new ArrayList<>();

        mainActivity = (MainActivity) getActivity();
        // laden begint
        mainActivity.mainProgressBar.setVisibility(View.VISIBLE);

        mainActivity.friendsTabs.setVisibility(View.VISIBLE);

        friendLongClickOptionsList[1] = "Chat history";
        friendLongClickOptionsList[2] = "Remove friend";
        pendingLongClickOptionsList[1] = "Accept";

        if (mainActivity.hasFriendListStored) {
            makeContactsLists(mainActivity.completeContactsListString);
        } else {
            new contactsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + thisUser + mainActivity.urlKey,
                    "https://fewgamers.com/api/userrelation/?user2=" + thisUser + mainActivity.urlKey);
        }

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder friendClickOptionsBuilder = new AlertDialog.Builder(getActivity());

                String[] optionsList;
                String openProfileOption = currentContactsList.get(position).getUsername() + "'s profile";
                friendLongClickOptionsList[0] = openProfileOption;
                pendingLongClickOptionsList[2] = openProfileOption;
                String thirdOption = "";
                switch (currentContactsList.get(position).getStatus()) {
                    case "FA":
                        thirdOption = "Remove friend";
                        break;
                    case "FP":
                        thirdOption = "Accept friend request";
                        break;
                    case "B":
                        thirdOption = "Unblock";
                        break;
                }
                friendLongClickOptionsList[2] = thirdOption;

                friendClickOptionsBuilder.setItems(friendLongClickOptionsList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openFriendProfile(position);
                        } else if (which == 1) {
                            openChatActivity();
                        }
                    }
                });

                AlertDialog friendClickOptionsDialog = friendClickOptionsBuilder.create();
                friendClickOptionsDialog.show();

                return true;
            }
        });

        conctactsTabsSelect = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (notReady) {
                    return;
                }
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
                        break;
                }
                friendAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };
        mainActivity.friendsTabs.addOnTabSelectedListener(conctactsTabsSelect);

        super.onViewCreated(view, savedInstanceState);
    }

    private void openFriendProfile(int position) {
        FragUserInfo fragment = new FragUserInfo();
        fragment.setFriendUUID(currentContactsList.get(position).getUsername());

        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            mainActivity.friendsTabs.setVisibility(View.GONE);
            ft.commit();
        }
    }

    private void openChatActivity() {
        Intent moveToChatIntent = new Intent(getActivity(), ChatActivity.class);
        startActivity(moveToChatIntent);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        openChatActivity();
    }


    private void makeContactsLists(String jsonString) {
        JSONArray jsonArray;

        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Server list not found", "Something went wrong when loading server data");
            return;
        }

        ArrayList<String[]> allRelations = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String[] relation = extractRelation(jsonArray.getJSONObject(i));
                if (!relation[0].equals("null")) {
                    allRelations.add(relation);
                }
            } catch (JSONException exception) {
                Log.e("Friend object missing", "Friend object data incomplete");
            }
        }
        contactsCount = allRelations.size();
        for (String[] relation : allRelations) {
            addContact(relation[1], selectContactsList(relation[0]));
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
        if (thisUser.equals(user1)) {
            if (relationStatus.equals("FP")) {
                relationStatus = "null";
            } else {
                contactUUID = user2;
            }
        } else if (thisUser.equals(user2)) {
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


    private void addContact(String contactUUID, final ArrayList<ContactObject> selectedList) {
        if (selectedList == null) {
            return;
        }
        contactsQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?uuid=" + contactUUID + mainActivity.urlKey, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ContactObject contactObject = new ContactObject();
                contactObject.defineContact(response);
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

    private class contactsAsyncTask extends AsyncTask<String, Void, String> {
        String response = "";

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL urlOne = new URL(strings[0]);
                URL urlTwo = new URL(strings[1]);

                HttpURLConnection connectionOne = (HttpURLConnection) urlOne.openConnection();
                HttpURLConnection connectionTwo = (HttpURLConnection) urlTwo.openConnection();

                connectionOne.setRequestMethod("GET");
                connectionTwo.setRequestMethod("GET");

                int responseCodeOne = connectionOne.getResponseCode();
                int responseCodeTwo = connectionTwo.getResponseCode();

                Log.d("Response Code 1", responseCodeOne + "");
                Log.d("Response Code 2", responseCodeTwo + "");

                BufferedReader readerOne = new BufferedReader(new InputStreamReader(connectionOne.getInputStream()));
                BufferedReader readerTwo = new BufferedReader(new InputStreamReader(connectionTwo.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();

                while ((line = readerOne.readLine()) != null) {
                    responseOutput.append(line);
                }
                while ((line = readerTwo.readLine()) != null) {
                    responseOutput.append(line);
                }

                response = responseOutput.toString();

                connectionOne.disconnect();
                connectionTwo.disconnect();

                Log.d("Response Output", response);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d("formattedstring", response);

            makeContactsLists(response);

            mainActivity.completeContactsListString = response;
            mainActivity.hasFriendListStored = true;
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
        friendAdapter = new FriendListAdapter(getActivity(), currentContactsList);
        setListAdapter(friendAdapter);

        // laden is voorbij
        mainActivity.mainProgressBar.setVisibility(View.GONE);

        notReady = false;
    }

    public void removeTabsSelector() {
        mainActivity.friendsTabs.removeOnTabSelectedListener(conctactsTabsSelect);
    }
}
