package com.fewgamers.fewgamers;

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

// this class is currently not in use, due to the absence of a chat feature in our service
public class ChatAdapter extends ArrayAdapter<ChatObject> {
    Context context;
    private ArrayList<ChatObject> chatList;
    private ChatObject thisChatobject;

    public ChatAdapter(Context context, ArrayList<ChatObject> chatList) {
        super(context, R.layout.chat_list_item, chatList);

        this.context = context;
        this.chatList = chatList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.chat_list_item, parent, false);

        thisChatobject = chatList.get(position);

        TextView message, timeOfDay, chatDateNotifier;

        if (thisChatobject.isADateNotifier()) {
            chatDateNotifier = (TextView) resView.findViewById(R.id.chatDateNotifier);
            chatDateNotifier.setText(getDateNotifierString(thisChatobject.getDate()));
            chatDateNotifier.setBackground(context.getResources().getDrawable(R.drawable.chat_date_rounded_textview));
            return resView;
        }

        if (chatList.get(position).isMessageFromMe().equals("true")) {
            message = (TextView) resView.findViewById(R.id.usMessage);
            timeOfDay = (TextView) resView.findViewById(R.id.usTime);
            resView.findViewById(R.id.chat_bubble_right_bg).setBackground(context.getResources().getDrawable(R.drawable.chat_bubble_nine_right));
        } else {
            message = (TextView) resView.findViewById(R.id.themMessage);
            timeOfDay = (TextView) resView.findViewById(R.id.themTime);
            resView.findViewById(R.id.chat_bubble_left_bg).setBackground(context.getResources().getDrawable(R.drawable.chat_bubble_nine_left));
        }
        message.setText(thisChatobject.getMessage());
        timeOfDay.setText(thisChatobject.getTimeOfDay());

        return resView;
    }

    private String getDateNotifierString(String dateString) {
        String month, day, year;
        switch (dateString.substring(0, 2)) {
            case "01":
                month = "January";
                break;
            case "02":
                month = "February";
                break;
            case "03":
                month = "March";
                break;
            case "04":
                month = "April";
                break;
            case "05":
                month = "May";
                break;
            case "06":
                month = "June";
                break;
            case "07":
                month = "July";
                break;
            case "08":
                month = "August";
                break;
            case "09":
                month = "September";
                break;
            case "10":
                month = "Oktober";
                break;
            case "11":
                month = "November";
                break;
            case "12":
                month = "December";
                break;
            default:
                month = null;
        }
        day = dateString.substring(3, 5);
        if (Integer.parseInt(day) < 10) {
            day = day.substring(1, 2);
        }
        year = dateString.substring(6, 10);
        if (month != null) {
            return month + " " + day + ", " + year;
        }
        else {
            return "";
        }
    }
}
