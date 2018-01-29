package com.fewgamers.fgmockup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/16/2017.
 */

// this class translates an ArrayList<ContactObject> to a visual display of a contacts list
public class ContactsListAdapter extends ArrayAdapter<ContactObject> {
    private final Activity context;
    private final ArrayList<ContactObject> contactList;

    // constructor that passes on the Activity's context, and the list from which a ListView is
    // populated
    ContactsListAdapter(Activity context, ArrayList<ContactObject> contactList) {
        super(context, R.layout.contact_list_item, contactList);

        this.context = context;
        this.contactList = contactList;
    }

    // method in which the fields of a ContactObject are added to a ListView item
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ContactObject thisContact = contactList.get(position);
        View listViewItem;

        // a header ContactObject is given a different layout file to work with. its header text is
        // set, and it is also made non-clickable
        if (thisContact.isAHeader()) {
            listViewItem = inflater.inflate(R.layout.conctacts_list_header, parent, false);
            listViewItem.setOnClickListener(null);
            ((TextView) listViewItem.findViewById(R.id.contactsListHeaderText)).setText(thisContact.getHeader());
            return listViewItem;
        }

        listViewItem = inflater.inflate(R.layout.contact_list_item, parent, false);

        String relationStatus = thisContact.getRelationStatus();
        String accountStatus = thisContact.getAccountStatus();

        ImageView contactImage = listViewItem.findViewById(R.id.contactImageView);
        TextView emailText, accountStatusText;
        emailText = (TextView) listViewItem.findViewById(R.id.contactEmail);
        accountStatusText = (TextView) listViewItem.findViewById(R.id.contactAccountStatus);

        // a blocked person is given a different image
        if (relationStatus.equals("B")) {
            ((TextView) listViewItem.findViewById(R.id.blockedName)).setText(thisContact.getUsername());
            contactImage.setImageResource(R.mipmap.ic_fg_blocked_contact);
        } else {
            ((TextView) listViewItem.findViewById(R.id.friendName)).setText(thisContact.getUsername());
            contactImage.setImageResource(R.mipmap.ic_fg_contact_icon);
            emailText.setText(thisContact.getEmail());
        }

        String accountStatusRead = "";
        switch (accountStatus) {
            case "A":
                accountStatusRead = "Active";
                break;
            case "U":
                accountStatusRead = "Inactive";
                break;
            case "B":
                accountStatusRead = "Banned";
                break;
        }
        accountStatusText.setText(accountStatusRead);

        return listViewItem;
    }

}
