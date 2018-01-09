package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragContacts extends ListFragBase {
    ArrayList<FriendObject> contactsList;

    String[] friendLongClickOptionsList = new String[2];

    FriendListAdapter friendAdapter;

    String thisUser = "dfaeb3cd-d796-48a0-a7c6-442cd1764dfa";

    MainActivity mainActivity;

    Map<String, ArrayList<FriendObject>> contactsListMap;

    TabLayout.OnTabSelectedListener conctactsTabsSelect;

    private boolean notReady = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragfriends, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        mainActivity.friendsTabs.setVisibility(View.VISIBLE);

        friendLongClickOptionsList[1] = "Chat history";

        if (mainActivity.hasFriendListStored) {
            extractContactsListFromString(mainActivity.completeContactsListString);
        } else {
            new friendsAsyncTask().execute("https://fewgamers.com/api/userrelation/?user1=" + thisUser, "https://fewgamers.com/api/userrelation/?user2=" + thisUser);
        }

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder friendClickOptionsBuilder = new AlertDialog.Builder(getActivity());

                friendLongClickOptionsList[0] = contactsList.get(position).getFriendName() + "'s profile";

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
                contactsList.clear();
                switch (tab.getPosition()) {
                    case 0:
                        contactsList.addAll(contactsListMap.get("friends"));
                        mainActivity.friendsTabSelected = 0;
                        break;
                    case 1:
                        contactsList.addAll(contactsListMap.get("pending"));
                        mainActivity.friendsTabSelected = 1;
                        break;
                    case 2:
                        contactsList.addAll(contactsListMap.get("blocked"));
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
        FragFriendsInfo fragment = new FragFriendsInfo();
        fragment.setFriendUUID(contactsList.get(position).getFriendName());

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


    private Map<String, ArrayList<FriendObject>> makeContactsLists(String jsonString) {
        JSONArray jsonArray;

        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Server list not found", "Something went wrong when loading server data");
            return null;
        }

        ArrayList<FriendObject> resFriends = new ArrayList<>();
        ArrayList<FriendObject> resBlocked = new ArrayList<>();
        ArrayList<FriendObject> resPending = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                FriendObject friendObject = new FriendObject();
                friendObject.defineFriend(jsonArray.getJSONObject(i), thisUser);

                switch (friendObject.getFriendStatus()) {
                    case "FA":
                        resFriends.add(friendObject);
                        break;
                    case "B":
                        resBlocked.add(friendObject);
                        break;
                    case "FP":
                        resPending.add(friendObject);
                        break;
                }
            } catch (JSONException exception) {
                Log.e("Friend object missing", "Friend object data incomplete");
            }
        }

        Map<String, ArrayList<FriendObject>> resMap = new HashMap<>();
        resMap.put("friends", resFriends);
        resMap.put("pending", resPending);
        resMap.put("blocked", resBlocked);

        return resMap;
    }

    private class friendsAsyncTask extends AsyncTask<String, Void, String> {
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
            String contactsListString = formatStringToJSONArray(response);
            Log.d("formattedstring", contactsListString);

            extractContactsListFromString(contactsListString);
        }
    }

    private void extractContactsListFromString(String contactsListString) {
        contactsListMap = makeContactsLists(contactsListString);

        switch (mainActivity.friendsTabSelected) {
            case 0:
                contactsList = new ArrayList<>(contactsListMap.get("friends"));
                break;
            case 1:
                contactsList = new ArrayList<>(contactsListMap.get("pending"));
                break;
            case 2:
                contactsList = new ArrayList<>(contactsListMap.get("blocked"));
                break;
        }

        friendAdapter = new FriendListAdapter(getActivity(), contactsList);
        setListAdapter(friendAdapter);

        notReady = false;

        mainActivity.completeContactsListString = contactsListString;
        mainActivity.hasFriendListStored = true;
    }

    public void removeTabsSelector() {
        mainActivity.friendsTabs.removeOnTabSelectedListener(conctactsTabsSelect);
    }
}
