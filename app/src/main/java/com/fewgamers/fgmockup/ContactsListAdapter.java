package com.fewgamers.fgmockup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/16/2017.
 */

public class ContactsListAdapter extends ArrayAdapter<ContactObject> {
    private final Activity context;
    private final ArrayList<ContactObject> friendList;

    public ContactsListAdapter(Activity context, ArrayList<ContactObject> friendList) {
        super(context, R.layout.contact_list_item, friendList);

        this.context = context;
        this.friendList = friendList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.contact_list_item, parent, false);
        final ContactObject thisFriend = friendList.get(position);
        String relationStatus = thisFriend.getRelationStatus();
        String status = thisFriend.getStatus();

        TextView friendName, friendEMail, friendStatus;
        friendName = (TextView) resView.findViewById(R.id.friendName);
        friendEMail = (TextView) resView.findViewById(R.id.friendEMail);
        friendStatus = (TextView) resView.findViewById(R.id.friendStatus);

        friendName.setText(thisFriend.getUsername());

        if (relationStatus.equals("FA")) {
            friendEMail.setText(thisFriend.getEmail());
        }

        friendStatus.setText(status);

        return resView;
    }

}
