package com.fewgamers.fgmockup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/16/2017.
 */

public class ChatAdapter extends ArrayAdapter<ChatObject> {
    Context context;
    private  ArrayList<ChatObject> chatList;

    public ChatAdapter(Context context, ArrayList<ChatObject> chatList) {
        super(context, R.layout.chat_list_item, chatList);

        this.context = context;
        this.chatList = chatList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.chat_list_item, parent, false);

        TextView message, timeOfDay;

        if (chatList.get(position).isMessageFromMe().equals("true")) {
            message = (TextView) resView.findViewById(R.id.usMessage);
            timeOfDay = (TextView) resView.findViewById(R.id.themTime);
        } else {
            message = (TextView) resView.findViewById(R.id.themMessage);
            timeOfDay = (TextView) resView.findViewById(R.id.themTime);
        }
        message.setText(chatList.get(position).getMessage());

        return resView;
    }

}
