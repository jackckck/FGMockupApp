package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragFriends extends ListFragBase {
    ArrayList<FriendObject> friendList;

    String[] friendLongClickOptionsList = new String[2];

    FriendListAdapter friendAdapter;

    RequestQueue friendRequestQueue;

    String friendListString;
    String thisUser = "dfaeb3cd-d796-48a0-a7c6-442cd1764dfa";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragfriends, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        friendLongClickOptionsList[1] = "Chat history";

        friendRequestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        makeStringRequest(friendRequestQueue, "http://fewgamers.com/api/userrelation/");

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder friendClickOptionsBuilder = new AlertDialog.Builder(getActivity());

                friendLongClickOptionsList[0] = friendList.get(position).getFriendName() + "'s profile";

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
        super.onViewCreated(view, savedInstanceState);
    }

    private void openFriendProfile(int position) {
        FragFriendsInfo fragment = new FragFriendsInfo();
        fragment.setFriendInfo(friendList.get(position));

        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
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


    private ArrayList<FriendObject> makeFriendList(String jsonString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Server list not found", "Something went wrong when loading server data");
        }

        ArrayList<FriendObject> res = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                FriendObject friendObject = new FriendObject();
                friendObject.defineFriend(jsonArray.getJSONObject(i), thisUser);

                if (friendObject.isMyFriend()) {
                    res.add(friendObject);
                }
            } catch (JSONException exception) {
                Log.e("Friend object missing", "Friend object data incomplete");
            }
        }

        return res;
    }

    private void makeStringRequest(RequestQueue queue, String url) {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                friendListString = formatStringToJSONArray(response);
                friendList = makeFriendList(friendListString);

                friendAdapter = new FriendListAdapter(getActivity(), friendList);
                setListAdapter(friendAdapter);

                Log.d("Opletten !!!", friendListString);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                friendListString = "error";
            }
        });

        queue.add(stringRequest);
    }
}
