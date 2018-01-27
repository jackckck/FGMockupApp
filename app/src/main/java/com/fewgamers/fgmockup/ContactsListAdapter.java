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
    private final ArrayList<ContactObject> contactList;

    public ContactsListAdapter(Activity context, ArrayList<ContactObject> contactList) {
        super(context, R.layout.contact_list_item, contactList);

        this.context = context;
        this.contactList = contactList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ContactObject thisContact = contactList.get(position);
        View resView;

        if (thisContact.isAHeader()) {
            resView = inflater.inflate(R.layout.conctacts_list_header, parent, false);
            resView.setOnClickListener(null);
            ((TextView) resView.findViewById(R.id.contactsListHeaderText)).setText(thisContact.getHeader());
            return resView;
        }

        resView = inflater.inflate(R.layout.contact_list_item, parent, false);


        String relationStatus = thisContact.getRelationStatus();
        String accountStatus = thisContact.getAccountStatus();

        TextView nameText, emailText, accountStatusText;
        nameText = (TextView) resView.findViewById(R.id.contactName);
        emailText = (TextView) resView.findViewById(R.id.contactEmail);
        accountStatusText = (TextView) resView.findViewById(R.id.contactAccountStatus);

        nameText.setText(thisContact.getUsername());

        if (!relationStatus.equals("B")) {
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

        return resView;
    }

}
