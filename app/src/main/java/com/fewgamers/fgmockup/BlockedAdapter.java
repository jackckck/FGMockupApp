package com.fewgamers.fgmockup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 1/3/2018.
 */

public class BlockedAdapter extends ArrayAdapter<String> {
    Context context;
    private ArrayList<String> blockedList;

    public BlockedAdapter(Context context, ArrayList<String> blockedList) {
        super(context, R.layout.blocked_list_item, blockedList);

        this.context = context;
        this.blockedList = blockedList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.blocked_list_item, parent, false);

        TextView blockedName = (TextView) resView.findViewById(R.id.blocked_name_textview);

        blockedName.setText(blockedList.get(position));

        return resView;
    }
}
