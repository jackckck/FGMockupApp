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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragFriends extends ListFragment {
    ArrayList<FriendObject> friendList;

    FriendListAdapter friendAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragfriends, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String jsonString = "[{\"name\":\"luuk123455\",\"email\":\"luuk123455@fewgamers.com\",\"status\":\"online\"},\n" +
                "{\"name\":\"ValentijnvanZwieten\",\"email\":\"valentijnvanzwieten@fewgamers.com\",\"status\":\"online\"},\n" +
                "{\"name\":\"Xander12345\",\"email\":\"xander12345@fewgamers.com\",\"status\":\"online\"},\n" +
                "{\"name\":\"Craz1k0ek\",\"email\":\"crazikoek@fewgamers.com\",\"status\":\"away\"},\n" +
                "{\"name\":\"koeninho\",\"email\":\"koeninho@fewgamers.com\",\"status\":\"away\"},\n" +
                "{\"name\":\"jackckck\",\"email\":\"j.d.dejong@students.com\",\"status\":\"offline\"}]";

        friendList = makeFriendList(jsonString);

        friendAdapter = new FriendListAdapter(getActivity(), friendList);
        setListAdapter(friendAdapter);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent moveToChatIntent = new Intent(getActivity(), ChatActivity.class);
        startActivity(moveToChatIntent);
    }

    private ArrayList<FriendObject> makeFriendList(String jsonString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Friend list not found", "Something went wrong when loading friend data");
        }

        ArrayList<FriendObject> res = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                FriendObject friendObject = new FriendObject();
                friendObject.defineFriend(jsonArray.getJSONObject(i));

                res.add(friendObject);
            }
            catch (JSONException exception) {
                Log.e("Friend object missing", "Friend object data incomplete");
            }
        }

        return res;
    }
}
