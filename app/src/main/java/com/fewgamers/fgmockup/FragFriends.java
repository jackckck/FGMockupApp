package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FragFriends extends ListFragment {
    ArrayList<FriendObject> friendList;

    FriendListAdapter friendAdapter;

    RequestQueue friendRequestQueue;

    String jsonString;
    String thisUser = "FGfanboy";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragfriends, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        friendRequestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        makeStringRequest(friendRequestQueue, "http://fewgamers.com/api/userrelation/?uuid=ALL");

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent moveToChatIntent = new Intent(getActivity(), ChatActivity.class);
        startActivity(moveToChatIntent);
    }

    private ArrayList<FriendObject> makeFriendList(String jsonString) {
        String[] jsonObjects = jsonString.split(".\n");

        ArrayList<FriendObject> res = new ArrayList<>();

        for (String jsonObjectString : jsonObjects) {
            try {
                FriendObject friendObject = new FriendObject();
                JSONObject jsonFriend = new JSONObject(jsonObjectString);

                String userOne, userTwo;
                userOne = jsonFriend.getString("user1");
                userTwo = jsonFriend.getString("user2");
                if (userOne.equals(thisUser)) {
                    friendObject.setFriendName(userOne);
                } else if (userTwo.equals(thisUser)) {
                    friendObject.setFriendName(userTwo);
                }

                friendObject.setFriendEMail("test@test.nl");
                friendObject.setFriendStatus("online");

                res.add(friendObject);
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
                jsonString = response;
                friendList = makeFriendList(jsonString);

                friendAdapter = new FriendListAdapter(getActivity(), friendList);
                setListAdapter(friendAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                jsonString = "error";
            }
        });

        queue.add(stringRequest);
    }
}
