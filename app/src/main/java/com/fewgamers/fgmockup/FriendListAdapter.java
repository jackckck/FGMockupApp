package com.fewgamers.fgmockup;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/16/2017.
 */

public class FriendListAdapter extends ArrayAdapter<FriendObject> {
    private final Activity context;
    private final ArrayList<FriendObject> friendList;

    public FriendListAdapter(Activity context, ArrayList<FriendObject> friendList) {
        super(context, R.layout.friends_list_item, friendList);

        this.context = context;
        this.friendList = friendList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.friends_list_item, parent, false);
        final FriendObject thisFriend = friendList.get(position);

        TextView friendName, friendEMail, friendStatus;
        friendName = (TextView) resView.findViewById(R.id.friendName);
        friendEMail = (TextView) resView.findViewById(R.id.friendEMail);
        friendStatus = (TextView) resView.findViewById(R.id.friendStatus);

        friendName.setText(thisFriend.getFriendName());
        //friendEMail.setText(thisFriend.getFriendEMail());
        friendStatus.setText(thisFriend.getFriendStatus());

        return resView;
    }

}
